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

package com.pepperonas.andlab;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import com.pepperonas.materialdialog.MaterialDialog;
import com.pepperonas.materialdialog.MaterialDialog.ShowListener;

/**
 * @author Martin Pfeffer
 * @see <a href="https://celox.io">https://celox.io</a>
 */
class DialogLargeButton {

    public DialogLargeButton(Context context) {
        new MaterialDialog.Builder(context)
            .positiveText("OK")
            .neutralText("NOT NOW")
            .negativeText("CANCEL")
            .title("The title")
            .message("The message.")
            .showListener(new ShowListener() {
                @Override
                public void onShow(AlertDialog dialog) {
                    super.onShow(dialog);
                    dialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextSize(36);
                    dialog.getButton(DialogInterface.BUTTON_NEUTRAL).setTextSize(24);
                    dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextSize(12);
                }
            }).show();
    }
}
