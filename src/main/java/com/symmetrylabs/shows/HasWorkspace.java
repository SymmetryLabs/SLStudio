package com.symmetrylabs.shows;

import com.symmetrylabs.slstudio.workspaces.Workspace;

/** Shows that have a workspace should implement this interface for the workspace switcher to show up on the sidebar. */
public interface HasWorkspace {
    Workspace getWorkspace();
}
