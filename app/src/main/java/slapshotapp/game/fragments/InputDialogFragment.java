package slapshotapp.game.fragments;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import slapshotapp.game.tictactoe.R;

/**
 * Created by Will on 7/6/2014.
 */
public class InputDialogFragment extends DialogFragment {
    protected final static String ARGUMENT_KEY_TITLE = "com.slapshotapp.game.tictactoe.title";
    protected final static String ARGUMENT_KEY_NAME = "com.slapshotapp.game.tictactoe.name";
    protected final static String ARGUMENT_PLAYER_NUMBER =
        "com.slapshotapp.game.tictactoe.playerNum";

    public interface ActionListener {
        public void onNameEntered(String name, int playerNumber);
    }

    protected ActionListener listener;
    protected int playerNumber;

    @BindView(R.id.fragment_edit_text) EditText editText;

    public static InputDialogFragment newInstance(String title, String currentName,
        int playerNumber) {
        InputDialogFragment fragment = new InputDialogFragment();

        Bundle arguments = new Bundle();

        if (title != null) {
            arguments.putString(ARGUMENT_KEY_TITLE, title);
        }

        if (currentName != null) {
            arguments.putString(ARGUMENT_KEY_NAME, currentName);
        }

        arguments.putInt(ARGUMENT_PLAYER_NUMBER, playerNumber);

        fragment.setArguments(arguments);

        return fragment;
    }

    @OnClick(R.id.done_button) void doneButtonClicked() {
        if (listener != null) {
            final String name = editText.getText().toString();
            listener.onNameEntered(name, playerNumber);
        }

        dismiss();
    }

    @OnClick(R.id.cancel_button) void cancelButtonClicked() {
        dismiss();
    }

    public InputDialogFragment() {

    }

    public void setListener(ActionListener listener) {
        this.listener = listener;
    }

    @Override public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog myDialog = super.onCreateDialog(savedInstanceState);

        if (getArguments().containsKey(ARGUMENT_KEY_TITLE)) {
            myDialog.setTitle(getArguments().getString(ARGUMENT_KEY_TITLE));
        }

        return myDialog;
    }

    @Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
        Bundle savedInstanceState) {
        View dialogView = inflater.inflate(R.layout.fragment_input_dialog, container, false);

        ButterKnife.bind(this, dialogView);

        if (getArguments().containsKey(ARGUMENT_KEY_NAME)) {
            final String name = getArguments().getString(ARGUMENT_KEY_NAME);
            editText.setText(name);
        }

        playerNumber = getArguments().getInt(ARGUMENT_PLAYER_NUMBER);

        return dialogView;
    }

    @Override public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE
                    || event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                    doneButtonClicked();
                    return true;
                }

                return false;
            }
        });

        getDialog().getWindow()
            .setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }
}
