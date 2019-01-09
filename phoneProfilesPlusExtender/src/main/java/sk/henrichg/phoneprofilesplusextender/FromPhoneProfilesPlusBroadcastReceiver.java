package sk.henrichg.phoneprofilesplusextender;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

class FromPhoneProfilesPlusBroadcastReceiver extends BroadcastReceiver {

    @SuppressLint("LongLogTag")
    @Override
    public void onReceive(Context context, Intent intent) {
        if ((intent == null) || (intent.getAction() == null))
            return;

        //Log.e("FromPhoneProfilesPlusBroadcastReceiver.onReceive", "received broadcast action="+intent.getAction());

        if (intent.getAction().equals(PPPEApplication.ACTION_REGISTER_PPPE_FUNCTION)) {
            String registrationApplication = intent.getStringExtra(PPPEApplication.EXTRA_REGISTRATION_APP);
            int registrationType = intent.getIntExtra(PPPEApplication.EXTRA_REGISTRATION_TYPE, 0);

            if (registrationApplication.equals("PhoneProfiles")) {
                switch (registrationType) {
                    case PPPEApplication.REGISTRATION_TYPE_FORCE_STOP_APPLICATIONS_REGISTER:
                        PPPEApplication.registeredForceStopApplicationsFunctionPP = true;
                    case PPPEApplication.REGISTRATION_TYPE_FORCE_STOP_APPLICATIONS_UNREGISTER:
                        PPPEApplication.registeredForceStopApplicationsFunctionPP = false;
                }
            }
            if (registrationApplication.equals("PhoneProfilesPlus")) {
                switch (registrationType) {
                    case PPPEApplication.REGISTRATION_TYPE_FORCE_STOP_APPLICATIONS_REGISTER:
                        PPPEApplication.registeredForceStopApplicationsFunctionPPP = true;
                    case PPPEApplication.REGISTRATION_TYPE_FORCE_STOP_APPLICATIONS_UNREGISTER:
                        PPPEApplication.registeredForceStopApplicationsFunctionPPP = false;
                    case PPPEApplication.REGISTRATION_TYPE_FOREGROUND_APPLICATION_REGISTER:
                        PPPEApplication.registeredForegroundApplicationFunctionPPP = true;
                    case PPPEApplication.REGISTRATION_TYPE_FOREGROUND_APPLICATION_UNREGISTER:
                        PPPEApplication.registeredForegroundApplicationFunctionPPP = false;
                    case PPPEApplication.REGISTRATION_TYPE_SMS_REGISTER:
                        PPPEApplication.registeredSMSFunctionPPP = true;
                    case PPPEApplication.REGISTRATION_TYPE_SMS_UNREGISTER:
                        PPPEApplication.registeredSMSFunctionPPP = false;
                    case PPPEApplication.REGISTRATION_TYPE_CALL_REGISTER:
                        PPPEApplication.registeredCallFunctionPPP = true;
                    case PPPEApplication.REGISTRATION_TYPE_CALL_UNREGISTER:
                        PPPEApplication.registeredCallFunctionPPP = false;
                }
            }

        }
        else
        if (intent.getAction().equals(PPPEAccessibilityService.ACTION_FORCE_STOP_APPLICATIONS_START)) {
            long profileId = intent.getLongExtra(ForceCloseIntentService.EXTRA_PROFILE_ID, 0);

            Intent scanServiceIntent = new Intent(context, ForceCloseIntentService.class);
            scanServiceIntent.putExtra(ForceCloseIntentService.EXTRA_APPLICATIONS, intent.getStringExtra(ForceCloseIntentService.EXTRA_APPLICATIONS));
            scanServiceIntent.putExtra(ForceCloseIntentService.EXTRA_PROFILE_ID, profileId);
            context.startService(scanServiceIntent);
        }
    }
}
