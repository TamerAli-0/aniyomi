package eu.kanade.tachiyomi

import android.app.Application
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import com.squareup.sqldelight.android.AndroidSqliteDriver
import data.History
import data.Mangas
import dataanime.Animehistory
import dataanime.Animes
import eu.kanade.data.AndroidAnimeDatabaseHandler
import eu.kanade.data.AndroidDatabaseHandler
import eu.kanade.data.AnimeDatabaseHandler
import eu.kanade.data.DatabaseHandler
import eu.kanade.data.dateAdapter
import eu.kanade.data.listOfStringsAdapter
import eu.kanade.tachiyomi.animesource.AnimeSourceManager
import eu.kanade.tachiyomi.data.cache.AnimeCoverCache
import eu.kanade.tachiyomi.data.cache.ChapterCache
import eu.kanade.tachiyomi.data.cache.CoverCache
import eu.kanade.tachiyomi.data.cache.EpisodeCache
import eu.kanade.tachiyomi.data.download.AnimeDownloadManager
import eu.kanade.tachiyomi.data.download.DownloadManager
import eu.kanade.tachiyomi.data.preference.PreferencesHelper
import eu.kanade.tachiyomi.data.saver.ImageSaver
import eu.kanade.tachiyomi.data.track.TrackManager
import eu.kanade.tachiyomi.data.track.job.DelayedTrackingStore
import eu.kanade.tachiyomi.extension.AnimeExtensionManager
import eu.kanade.tachiyomi.extension.ExtensionManager
import eu.kanade.tachiyomi.mi.AnimeDatabase
import eu.kanade.tachiyomi.network.NetworkHelper
import eu.kanade.tachiyomi.source.SourceManager
import io.requery.android.database.sqlite.RequerySQLiteOpenHelperFactory
import kotlinx.serialization.json.Json
import uy.kohesive.injekt.api.InjektModule
import uy.kohesive.injekt.api.InjektRegistrar
import uy.kohesive.injekt.api.addSingleton
import uy.kohesive.injekt.api.addSingletonFactory
import uy.kohesive.injekt.api.get

class AppModule(val app: Application) : InjektModule {

    override fun InjektRegistrar.registerInjectables() {
        addSingleton(app)

        val sqlDriverManga = AndroidSqliteDriver(
            schema = Database.Schema,
            context = app,
            name = "tachiyomi.db",
            factory = if (BuildConfig.DEBUG && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Support database inspector in Android Studio
                FrameworkSQLiteOpenHelperFactory()
            } else {
                RequerySQLiteOpenHelperFactory()
            },
        )

        val sqlDriverAnime = AndroidSqliteDriver(
            schema = AnimeDatabase.Schema,
            context = app,
            name = "tachiyomi.animedb",
            factory = if (BuildConfig.DEBUG && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Support database inspector in Android Studio
                FrameworkSQLiteOpenHelperFactory()
            } else {
                RequerySQLiteOpenHelperFactory()
            },
        )

        addSingletonFactory {
            Database(
                driver = sqlDriverManga,
                historyAdapter = History.Adapter(
                    last_readAdapter = dateAdapter,
                ),
                mangasAdapter = Mangas.Adapter(
                    genreAdapter = listOfStringsAdapter,
                ),
            )
        }

        addSingletonFactory {
            AnimeDatabase(
                driver = sqlDriverAnime,
                animehistoryAdapter = Animehistory.Adapter(
                    last_seenAdapter = dateAdapter,
                ),
                animesAdapter = Animes.Adapter(
                    genreAdapter = listOfStringsAdapter,
                ),
            )
        }

        addSingletonFactory<DatabaseHandler> { AndroidDatabaseHandler(get(), sqlDriverManga) }

        addSingletonFactory<AnimeDatabaseHandler> { AndroidAnimeDatabaseHandler(get(), sqlDriverAnime) }

        addSingletonFactory { Json { ignoreUnknownKeys = true } }

        addSingletonFactory { PreferencesHelper(app) }

        addSingletonFactory { ChapterCache(app) }

        addSingletonFactory { EpisodeCache(app) }

        addSingletonFactory { CoverCache(app) }

        addSingletonFactory { AnimeCoverCache(app) }

        addSingletonFactory { NetworkHelper(app) }

        addSingletonFactory { SourceManager(app).also { get<ExtensionManager>().init(it) } }

        addSingletonFactory { AnimeSourceManager(app).also { get<AnimeExtensionManager>().init(it) } }

        addSingletonFactory { ExtensionManager(app) }

        addSingletonFactory { AnimeExtensionManager(app) }

        addSingletonFactory { DownloadManager(app) }

        addSingletonFactory { AnimeDownloadManager(app) }

        addSingletonFactory { TrackManager(app) }

        addSingletonFactory { DelayedTrackingStore(app) }

        addSingletonFactory { ImageSaver(app) }

        // Asynchronously init expensive components for a faster cold start
        ContextCompat.getMainExecutor(app).execute {
            get<PreferencesHelper>()

            get<NetworkHelper>()

            get<SourceManager>()
            get<AnimeSourceManager>()

            get<Database>()
            get<AnimeDatabase>()

            get<DownloadManager>()
            get<AnimeDownloadManager>()
        }
    }
}
