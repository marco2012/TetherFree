package dbswear.r00t.me.tetherfree;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.scottyab.rootbeer.RootBeer;

public class MainActivity extends AppCompatActivity {

    private TextView textView, textView3;
    private ToggleButton toggle;
    private String hotspot_status, dummy;
    private CoordinatorLayout coordinatorLayout;
    private ProgressDialog dialog;

    private static final String TETHER_STATUS = "settings get global tether_dun_required";
    private static final String TETHER_ENABLE = "settings put global tether_dun_required 0";
    private static final String TETHER_DISABLE = "settings put global tether_dun_required null";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = (TextView) findViewById(R.id.textView);
        textView3 = (TextView) findViewById(R.id.textView3);
        toggle = (ToggleButton) findViewById(R.id.toggleButton);
        //root check
        RootBeer rootBeer = new RootBeer(this);
        if (!rootBeer.isRooted())
            startActivity(new Intent(MainActivity.this, NoRootActivity.class));

        //returns current hotspot status
        hotspot_status = RootCommandExec.sudoForResult(TETHER_STATUS);
        checkHotspotStatus(hotspot_status);

        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) { // The toggle is enabled
                    new AsyncTether().execute(0); //0 means enable
                } else {
                    new AsyncTether().execute(1); //1 means disable
                }
            }
        });
    }

    private void checkHotspotStatus(String hotspot_status) {
        if (hotspot_status.equals("null") || hotspot_status.equals("1")){ //paid hotspot
            toggle.setChecked(false);
            textView.setText(getString(R.string.hotspot_not_free));
            textView.setTextColor(Color.RED);
            toggle.setTextOff(getString(R.string.paying));
            Log.d("HOTSPOT STATUS", hotspot_status);
        } else if (hotspot_status.equals("0")) { //free
            toggle.setChecked(true);
            textView.setText(getString(R.string.hotspot_free));
            textView.setTextColor(Color.GREEN);
            toggle.setTextOn(getString(R.string.free));
            Log.d("HOTSPOT STATUS", hotspot_status);
        }
    }
    private void makeRebootSnackbar(){
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);
        Snackbar snackbar = Snackbar
                .make(coordinatorLayout, R.string.reboot_device, 5000)
                .setAction("RIAVVIA", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        makeToast(getString(R.string.rebooting));
                        dummy = RootCommandExec.sudoForResult("reboot"); //reboot device using root
                    }
                });
        snackbar.show();
    }

    private class AsyncTether extends AsyncTask<Integer, Void, Boolean> {
        @Override
        protected void onPreExecute(){
            super.onPreExecute();
            dialog = new ProgressDialog(MainActivity.this);
            // Set progress dialog title
            dialog.setTitle(R.string.loading);
            // Set progress dialog message
            if (toggle.isChecked()) //ON
                dialog.setMessage(getString(R.string.enabling_hotspot));
            else dialog.setMessage(getString(R.string.disabling_hotspot));
            // Show progress dialog
            dialog.show();
        }

        @Override
        protected void onProgressUpdate(Void[] values) {
        };

        @Override
        protected Boolean doInBackground(Integer... params) {
            if(params[0]==0)
                dummy = RootCommandExec.sudoForResult(TETHER_ENABLE);
            else if (params[0]==1)
                dummy = RootCommandExec.sudoForResult(TETHER_DISABLE);

            hotspot_status = RootCommandExec.sudoForResult(TETHER_STATUS);
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            if(dialog.isShowing()) dialog.dismiss();
            checkHotspotStatus(hotspot_status);
            makeRebootSnackbar();
        }

        @Override
        protected void onCancelled() {
            makeToast(getString(R.string.error_procedure));
        }
    }

    public void makeToast(String s){
        Toast.makeText(getApplicationContext(),s,Toast.LENGTH_SHORT).show();
    }
}
