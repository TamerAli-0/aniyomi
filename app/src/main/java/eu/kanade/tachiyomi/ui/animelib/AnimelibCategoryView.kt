package eu.kanade.tachiyomi.ui.animelib

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import androidx.recyclerview.widget.LinearLayoutManager
import dev.chrisbanes.insetter.applyInsetter
import eu.davidea.flexibleadapter.FlexibleAdapter
import eu.davidea.flexibleadapter.SelectableAdapter
import eu.kanade.domain.anime.model.Anime
import eu.kanade.domain.category.model.Category
import eu.kanade.tachiyomi.R
import eu.kanade.tachiyomi.data.animelib.AnimelibUpdateService
import eu.kanade.tachiyomi.data.database.models.toDomainAnime
import eu.kanade.tachiyomi.databinding.AnimelibCategoryBinding
import eu.kanade.tachiyomi.ui.main.MainActivity
import eu.kanade.tachiyomi.util.lang.plusAssign
import eu.kanade.tachiyomi.util.system.toast
import eu.kanade.tachiyomi.util.view.inflate
import eu.kanade.tachiyomi.util.view.onAnimationsFinished
import eu.kanade.tachiyomi.widget.AutofitRecyclerView
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import reactivecircus.flowbinding.recyclerview.scrollStateChanges
import reactivecircus.flowbinding.swiperefreshlayout.refreshes
import rx.subscriptions.CompositeSubscription
import java.util.ArrayDeque

/**
 * Fragment containing the animelib anime for a certain category.
 */
class AnimelibCategoryView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
    FrameLayout(context, attrs),
    FlexibleAdapter.OnItemClickListener,
    FlexibleAdapter.OnItemLongClickListener {

    private val scope = MainScope()

    /**
     * The fragment containing this view.
     */
    private lateinit var controller: AnimelibController

    /**
     * Category for this view.
     */
    lateinit var category: Category
        private set

    /**
     * Recycler view of the list of anime.
     */
    private lateinit var recycler: AutofitRecyclerView

    /**
     * Adapter to hold the anime in this category.
     */
    private lateinit var adapter: AnimelibCategoryAdapter

    /**
     * Subscriptions while the view is bound.
     */
    private var subscriptions = CompositeSubscription()

    private var lastClickPositionStack = ArrayDeque(listOf(-1))

    fun onCreate(controller: AnimelibController, binding: AnimelibCategoryBinding, viewType: Int) {
        this.controller = controller

        recycler = if (viewType == AnimelibAdapter.LIST_DISPLAY_MODE) {
            (binding.swipeRefresh.inflate(R.layout.library_list_recycler) as AutofitRecyclerView).apply {
                spanCount = 1
            }
        } else {
            (binding.swipeRefresh.inflate(R.layout.library_grid_recycler) as AutofitRecyclerView).apply {
                spanCount = controller.animePerRow
            }
        }

        recycler.applyInsetter {
            type(navigationBars = true) {
                padding()
            }
        }

        adapter = AnimelibCategoryAdapter(this)

        recycler.setHasFixedSize(true)
        recycler.adapter = adapter
        binding.swipeRefresh.addView(recycler)
        adapter.fastScroller = binding.fastScroller

        recycler.scrollStateChanges()
            .onEach {
                // Disable swipe refresh when view is not at the top
                val firstPos = (recycler.layoutManager as LinearLayoutManager)
                    .findFirstCompletelyVisibleItemPosition()
                binding.swipeRefresh.isEnabled = firstPos <= 0
            }
            .launchIn(scope)

        recycler.onAnimationsFinished {
            (controller.activity as? MainActivity)?.ready = true
        }

        // Double the distance required to trigger sync
        binding.swipeRefresh.setDistanceToTriggerSync((2 * 64 * resources.displayMetrics.density).toInt())
        binding.swipeRefresh.refreshes()
            .onEach {
                if (AnimelibUpdateService.start(context, category)) {
                    context.toast(R.string.updating_category)
                }

                // It can be a very long operation, so we disable swipe refresh and show a toast.
                binding.swipeRefresh.isRefreshing = false
            }
            .launchIn(scope)
    }

    fun onBind(category: Category) {
        this.category = category

        adapter.mode = if (controller.selectedAnimes.isNotEmpty()) {
            SelectableAdapter.Mode.MULTI
        } else {
            SelectableAdapter.Mode.SINGLE
        }

        subscriptions += controller.searchRelay
            .doOnNext { adapter.setFilter(it) }
            .skip(1)
            .subscribe { adapter.performFilter() }

        subscriptions += controller.animelibAnimeRelay
            .subscribe { onNextAnimelibAnime(it) }

        subscriptions += controller.selectionRelay
            .subscribe { onSelectionChanged(it) }

        subscriptions += controller.selectAllRelay
            .filter { it == category.id }
            .subscribe {
                adapter.currentItems.forEach { item ->
                    controller.setSelection(item.anime.toDomainAnime()!!, true)
                }
                controller.invalidateActionMode()
            }

        subscriptions += controller.selectInverseRelay
            .filter { it == category.id }
            .subscribe {
                adapter.currentItems.forEach { item ->
                    controller.toggleSelection(item.anime.toDomainAnime()!!)
                }
                controller.invalidateActionMode()
            }
    }

    fun onRecycle() {
        adapter.setItems(emptyList())
        adapter.clearSelection()
        unsubscribe()
    }

    fun onDestroy() {
        unsubscribe()
        scope.cancel()
    }

    private fun unsubscribe() {
        subscriptions.clear()
    }

    /**
     * Subscribe to [AnimelibAnimeEvent]. When an event is received, it updates the content of the
     * adapter.
     *
     * @param event the event received.
     */
    private fun onNextAnimelibAnime(event: AnimelibAnimeEvent) {
        // Get the anime list for this category.
        val animeForCategory = event.getAnimeForCategory(category).orEmpty()

        // Update the category with its anime.
        adapter.setItems(animeForCategory)

        if (adapter.mode == SelectableAdapter.Mode.MULTI) {
            controller.selectedAnimes.forEach { anime ->
                val position = adapter.indexOf(anime)
                if (position != -1 && !adapter.isSelected(position)) {
                    adapter.toggleSelection(position)
                    (recycler.findViewHolderForItemId(anime.id) as? AnimelibHolder<*>)?.toggleActivation()
                }
            }
        }
    }

    /**
     * Subscribe to [AnimelibSelectionEvent]. When an event is received, it updates the selection
     * depending on the type of event received.
     *
     * @param event the selection event received.
     */
    private fun onSelectionChanged(event: AnimelibSelectionEvent) {
        when (event) {
            is AnimelibSelectionEvent.Selected -> {
                if (adapter.mode != SelectableAdapter.Mode.MULTI) {
                    adapter.mode = SelectableAdapter.Mode.MULTI
                }
                findAndToggleSelection(event.anime)
            }
            is AnimelibSelectionEvent.Unselected -> {
                findAndToggleSelection(event.anime)

                with(adapter.indexOf(event.anime)) {
                    if (this != -1) lastClickPositionStack.remove(this)
                }

                if (controller.selectedAnimes.isEmpty()) {
                    adapter.mode = SelectableAdapter.Mode.SINGLE
                }
            }
            is AnimelibSelectionEvent.Cleared -> {
                adapter.mode = SelectableAdapter.Mode.SINGLE
                adapter.clearSelection()

                lastClickPositionStack.clear()
                lastClickPositionStack.push(-1)
            }
        }
    }

    /**
     * Toggles the selection for the given anime and updates the view if needed.
     *
     * @param anime the anime to toggle.
     */
    private fun findAndToggleSelection(anime: Anime) {
        val position = adapter.indexOf(anime)
        if (position != -1) {
            adapter.toggleSelection(position)
            (recycler.findViewHolderForItemId(anime.id) as? AnimelibHolder<*>)?.toggleActivation()
        }
    }

    /**
     * Called when a anime is clicked.
     *
     * @param position the position of the element clicked.
     * @return true if the item should be selected, false otherwise.
     */
    override fun onItemClick(view: View?, position: Int): Boolean {
        // If the action mode is created and the position is valid, toggle the selection.
        val item = adapter.getItem(position) ?: return false
        return if (adapter.mode == SelectableAdapter.Mode.MULTI) {
            if (adapter.isSelected(position)) {
                lastClickPositionStack.remove(position)
            } else {
                lastClickPositionStack.push(position)
            }
            toggleSelection(position)
            true
        } else {
            openAnime(item.anime.toDomainAnime()!!)
            false
        }
    }

    /**
     * Called when a anime is long clicked.
     *
     * @param position the position of the element clicked.
     */
    override fun onItemLongClick(position: Int) {
        controller.createActionModeIfNeeded()
        val lastClickPosition = lastClickPositionStack.peek()!!
        when {
            lastClickPosition == -1 -> setSelection(position)
            lastClickPosition > position ->
                for (i in position until lastClickPosition)
                    setSelection(i)
            lastClickPosition < position ->
                for (i in lastClickPosition + 1..position)
                    setSelection(i)
            else -> setSelection(position)
        }
        if (lastClickPosition != position) {
            lastClickPositionStack.remove(position)
            lastClickPositionStack.push(position)
        }
    }

    /**
     * Opens a anime.
     *
     * @param anime the anime to open.
     */
    private fun openAnime(anime: Anime) {
        controller.openAnime(anime)
    }

    /**
     * Tells the presenter to toggle the selection for the given position.
     *
     * @param position the position to toggle.
     */
    private fun toggleSelection(position: Int) {
        val item = adapter.getItem(position) ?: return

        controller.setSelection(item.anime.toDomainAnime()!!, !adapter.isSelected(position))
        controller.invalidateActionMode()
    }

    /**
     * Tells the presenter to set the selection for the given position.
     *
     * @param position the position to toggle.
     */
    private fun setSelection(position: Int) {
        val item = adapter.getItem(position) ?: return

        controller.setSelection(item.anime.toDomainAnime()!!, true)
        controller.invalidateActionMode()
    }
}
