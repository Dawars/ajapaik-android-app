package ee.ajapaik.android;

import com.facebook.FacebookSdk;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

import ee.ajapaik.android.util.WebImage;
import ee.ajapaik.android.test.BuildConfig;
import ee.ajapaik.android.test.R;

@ReportsCrashes(formKey="",
        mode = ReportingInteractionMode.DIALOG,
        mailTo = "reports@ajapaik.ee",
        resDialogText = R.string.acra_dialog_text,
        resDialogTitle = R.string.acra_dialog_title,
        resDialogCommentPrompt = R.string.acra_dialog_comment_prompt,
        resDialogOkToast = R.string.acra_dialog_ok_toast)
public class Application extends android.app.Application {
    @Override
    public void onCreate() {
        super.onCreate();

        if(!BuildConfig.DEBUG) {
            ACRA.init(this);
        }

        FacebookSdk.sdkInitialize(this);
        WebImage.invalidate(this);
    }
}