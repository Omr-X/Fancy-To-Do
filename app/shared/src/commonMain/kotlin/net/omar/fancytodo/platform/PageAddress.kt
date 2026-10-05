package net.omar.fancytodo.platform

/**
 * Host name the page was opened from ("localhost" on this PC, the PC's network address on a phone
 * or tablet). The API server runs on the same machine, so requests go to this host.
 */
expect fun pageHostname(): String
