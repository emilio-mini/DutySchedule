package me.emiliomini.dutyschedule.shared

/**
 * The endpoints an install starts out with before anyone has been through onboarding. Android ships
 * the production hosts, while iOS points at a demo backend; the production hosts are declared in
 * the Android source set alone so they are absent from the iOS binary entirely.
 */
expect val defaultPrepUrl: String

/** @see defaultPrepUrl */
expect val defaultDocscedUrl: String
