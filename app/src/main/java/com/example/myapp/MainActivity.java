package com.example.myapp;
import android.content.Intent;
import android.widget.Button;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import com.example.myapp.api.UserHelper;
import com.example.myapp.base.BaseActivity;
import com.example.myapp.profil.ProfilActivity;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.material.snackbar.Snackbar;

import java.util.Arrays;
import butterknife.BindView;
import butterknife.OnClick;


public class MainActivity extends BaseActivity {

    private static final int RC_SIGN_IN = 123;

    @BindView(R.id.main_activity_coordinator_layout)
    CoordinatorLayout coordinatorLayout;

    @BindView(R.id.login)
    Button login_button;

    @Override
    public int getFragmentLayout() {
        return R.layout.activity_main;
    }


    @OnClick(R.id.login)
    public void onClickLoginBtn()
    {
        if (!this.isCurrentUserLogged())
        { this.startConnexionActivity();}else
        {
            this.startProfileActivity();
        }

    }

    @Override
    protected void onResume()
    {
        super.onResume();
        updateUiAfterLogin();
    }

    public void startConnexionActivity()
    {
        startActivityForResult(
                AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setTheme(R.style.LoginTheme)
                .setAvailableProviders(Arrays.asList(new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build(),
                        new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build()))
                .setIsSmartLockEnabled(false, true)
                .setLogo(R.drawable.logo)
                .build(), RC_SIGN_IN);
    }

    private void showSnackBar( CoordinatorLayout coordinatorLayout, String message){
        Snackbar.make(coordinatorLayout, message, Snackbar.LENGTH_LONG).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        this.handleResponseAfterSignIn(requestCode, resultCode, data);
    }

    private void handleResponseAfterSignIn(int requestCode, int resultCode, Intent data){

        IdpResponse response = IdpResponse.fromResultIntent(data);

        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) { // SUCCESS
                createUserInFireStore();
                showSnackBar(this.coordinatorLayout, getString(R.string.connection_succeed));
            } else { // ERRORS
                if (response == null) {
                    showSnackBar(this.coordinatorLayout, getString(R.string.error_authentication_canceled));
                } else if (response.getErrorCode() == ErrorCodes.NO_NETWORK) {
                    showSnackBar(this.coordinatorLayout, getString(R.string.error_no_internet));
                } else if (response.getErrorCode() == ErrorCodes.UNKNOWN_ERROR) {
                    showSnackBar(this.coordinatorLayout, getString(R.string.error_unknown_error));
                }
            }
        }
    }


    public void updateUiAfterLogin()
    {
        login_button.setText(this.isCurrentUserLogged() ? getString(R.string.button_login_text_logged) : getString(R.string.button_login_text_not_logged));
    }

    // on cree un utilisateur dans FireStore
    public void createUserInFireStore()
    {
        if(this.getCurrentUser() != null)
        {
            String urlProfil = (this.getCurrentUser().getPhotoUrl() != null) ? this.getCurrentUser().getPhotoUrl().toString(): null;
            String id = this.getCurrentUser().getUid();
            String username = this.getCurrentUser().getDisplayName();

            UserHelper.createUser(id, username , urlProfil);
        }
    }

    private void startProfileActivity(){
        Intent intent = new Intent(this, ProfilActivity.class);
        startActivity(intent);
    }


}
