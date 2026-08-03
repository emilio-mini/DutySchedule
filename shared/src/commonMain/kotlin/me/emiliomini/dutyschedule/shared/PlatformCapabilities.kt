package me.emiliomini.dutyschedule.shared

/**
 * Whether the platform can wake the app to ring a duty alarm. iOS has no equivalent of an exact
 * alarm driving a foreground service, so the alarm UI is hidden there rather than shown inert.
 */
expect val supportsAlarms: Boolean
