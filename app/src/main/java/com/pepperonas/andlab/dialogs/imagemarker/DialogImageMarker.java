/*
 * Copyright (c) 2017 Martin Pfeffer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.pepperonas.andlab.dialogs.imagemarker;

import android.app.AlertDialog;
import android.content.Context;
import android.widget.Toast;
import com.pepperonas.andlab.R;
import com.pepperonas.materialdialog.MaterialDialog;
import com.pepperonas.materialdialog.MaterialDialog.ButtonCallback;
import com.pepperonas.materialdialog.MaterialDialog.ShowListener;

/**
 * @author Martin Pfeffer
 * @see <a href="https://celox.io">https://celox.io</a>
 */
public class DialogImageMarker {

    public DialogImageMarker(final Context context) {
        new MaterialDialog.Builder(context)
            .title("Set marker")
            .positiveText("OK")
            .customView(R.layout.dialog_image_marker)
            .showListener(new ShowListener() {
                @Override
                public void onShow(AlertDialog dialog) {
                    super.onShow(dialog);
                }
            })
            .buttonCallback(new ButtonCallback() {
                @Override
                public void onPositive(MaterialDialog dialog) {
                    super.onPositive(dialog);
                    MarkerImageView miv = (MarkerImageView) dialog
                        .findViewById(R.id.marker_image_view);
                    int x = miv.getPosX();
                    int y = miv.getPosY();
                    Toast.makeText(context,
                        "X: " + fmt(x,4) + " [" + fmt(miv.getRelativeX(),3) + "%]" + "\n"
                            + "Y: " + fmt(y,4) + " [" + fmt(miv.getRelativeY(),3) + "%]",
                        Toast.LENGTH_LONG).show();
                }
            })
            .show();
    }

    private String fmt(int number, int places) {
        return String.format("%0" + places + "d", number);
    }

}
