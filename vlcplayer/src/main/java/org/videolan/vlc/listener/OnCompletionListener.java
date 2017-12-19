package org.videolan.vlc.listener;

import org.videolan.libvlc.MediaPlayer;

/**
 * Author：caoyamin
 * Time: 2017/1/23
 * Email：yamin.cao@whatsmode.com
 * Copyright(c)2017 @Babeeq
 */

public interface OnCompletionListener {
    /**
     * Called when the end of a media source is reached during playback.
     *
     * @param mp the MediaPlayer that reached the end of the file
     */
    void onCompletion(MediaPlayer mp);
}
