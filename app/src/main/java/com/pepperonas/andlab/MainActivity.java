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

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import com.pepperonas.andlab.dialogs.DialogChartView;
import com.pepperonas.andlab.dialogs.imagemarker.DialogImageMarker;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        new DialogChartView(this);
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_dialog_image_marker:
                new DialogImageMarker(this);
                break;
            case R.id.btn_dialog_large_button:
                new DialogLargeButton(this);
                break;
            case R.id.btn_dialog_chart:
                new DialogChartView(this);
                break;
        }
    }

}
