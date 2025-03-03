package eu.kanade.tachiyomi.data.preference

/**
 * This class stores the keys for the preferences in the application.
 */
object PreferenceKeys {

    const val confirmExit = "pref_confirm_exit"

    const val showReadingMode = "pref_show_reading_mode"

    const val defaultReadingMode = "pref_default_reading_mode_key"

    const val preserveReadingPosition = "pref_preserve_reading_position"

    const val preserveWatchingPosition = "pref_preserve_watching_position"

    const val pipEpisodeToasts = "pref_pip_episode_toasts"

    const val pipOnExit = "pref_pip_on_exit"

    const val mpvConf = "pref_mpv_conf"

    const val defaultOrientationType = "pref_default_orientation_type_key"

    const val defaultPlayerOrientationType = "pref_default_player_orientation_type_key"

    const val adjustOrientationVideoDimensions = "pref_adjust_orientation_video_dimensions"

    const val defaultPlayerOrientationLandscape = "pref_default_player_orientation_landscape_key"

    const val defaultPlayerOrientationPortrait = "pref_default_player_orientation_portrait_key"

    const val playerSpeed = "pref_player_speed"

    const val playerSmoothSeek = "pref_player_smooth_seek"

    const val playerViewMode = "pref_player_view_mode"

    const val progressPreference = "pref_progress_preference"

    const val defaultIntroLength = "pref_default_intro_length"

    const val skipLengthPreference = "pref_skip_length_preference"

    const val alwaysUseExternalPlayer = "pref_always_use_external_player"

    const val externalPlayerPreference = "external_player_preference"

    const val autoUpdateTrack = "pref_auto_update_manga_sync_key"

    const val useExternalDownloader = "use_external_downloader"

    const val externalDownloaderSelection = "external_downloader_selection"

    const val downloadOnlyOverWifi = "pref_download_only_over_wifi_key"

    const val folderPerManga = "create_folder_per_manga"

    const val folderPerAnime = "create_folder_per_anime"

    const val removeAfterReadSlots = "remove_after_read_slots"

    const val removeAfterMarkedAsRead = "pref_remove_after_marked_as_read_key"

    const val defaultUserAgent = "default_user_agent"

    const val removeBookmarkedChapters = "pref_remove_bookmarked"

    const val filterDownloaded = "pref_filter_library_downloaded"

    const val filterUnread = "pref_filter_library_unread"

    const val filterStarted = "pref_filter_library_started"

    const val filterCompleted = "pref_filter_library_completed"

    const val filterTracked = "pref_filter_library_tracked"

    const val librarySortingMode = "library_sorting_mode"
    const val librarySortingDirection = "library_sorting_ascending"

    const val migrationSortingMode = "pref_migration_sorting"
    const val migrationSortingDirection = "pref_migration_direction"

    const val bottomNavStyle = "bottom_nav_style"

    const val startScreen = "start_screen"

    const val hideNotificationContent = "hide_notification_content"

    const val autoUpdateMetadata = "auto_update_metadata"

    const val autoUpdateTrackers = "auto_update_trackers"

    const val downloadNew = "download_new"

    const val dateFormat = "app_date_format"

    const val defaultCategory = "default_category"
    const val defaultAnimeCategory = "default_anime_category"

    const val skipRead = "skip_read"

    const val skipFiltered = "skip_filtered"

    const val searchPinnedSourcesOnly = "search_pinned_sources_only"

    const val dohProvider = "doh_provider"

    const val defaultChapterFilterByRead = "default_chapter_filter_by_read"

    const val defaultChapterFilterByDownloaded = "default_chapter_filter_by_downloaded"

    const val defaultChapterFilterByBookmarked = "default_chapter_filter_by_bookmarked"

    const val defaultChapterSortBySourceOrNumber = "default_chapter_sort_by_source_or_number" // and upload date

    const val defaultChapterSortByAscendingOrDescending = "default_chapter_sort_by_ascending_or_descending"

    const val defaultChapterDisplayByNameOrNumber = "default_chapter_display_by_name_or_number"

    const val defaultEpisodeFilterByRead = "default_episode_filter_by_read"

    const val defaultEpisodeFilterByDownloaded = "default_episode_filter_by_downloaded"

    const val defaultEpisodeFilterByBookmarked = "default_episode_filter_by_bookmarked"

    const val defaultEpisodeSortBySourceOrNumber = "default_episode_sort_by_source_or_number" // and upload date

    const val defaultEpisodeSortByAscendingOrDescending = "default_episode_sort_by_ascending_or_descending"

    const val defaultEpisodeDisplayByNameOrNumber = "default_episode_display_by_name_or_number"

    const val verboseLogging = "verbose_logging"

    const val autoClearChapterCache = "auto_clear_chapter_cache"

    fun trackUsername(syncId: Long) = "pref_mangasync_username_$syncId"

    fun trackPassword(syncId: Long) = "pref_mangasync_password_$syncId"

    fun trackToken(syncId: Long) = "track_token_$syncId"
}
