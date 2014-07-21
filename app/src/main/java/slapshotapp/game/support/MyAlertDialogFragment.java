package slapshotapp.game.support;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;


public class MyAlertDialogFragment extends DialogFragment
{
	public final static int NO_BUTTON = 0;
	
	private final static String TITLE_KEY = "title";
	private final static String BUTTON_ONE_KEY = "button_1";
	private final static String BUTTON_TWO_KEY = "button_2";
	private final static String DIALOG_MESSAGE_KEY = "message";
	private final static String DEFAULT_TITLE = "Untitled";
	private final static String DEFAULT_TITLE_BUTTON_ONE = "BUTTON 1";
	private final static String DEFAULT_TITLE_BUTTON_TWO = "BUTTON 2";
	
	
	/*
	 * Create an instance of the alert dialog
	 * 
	 * @param titleID Resource ID used to get the dialog title
	 * @param buttonOneTitleID Resource ID used to get a title for button 1
	 * @param buttonTwoTitleID Resource ID used to get a title for button 2 (NO_BUTTON for no button 2)
	 * @param dialogMessage A String for the dialog message
	 * 
	 * @return the newly created dialog instance
	 */
	public static MyAlertDialogFragment newInstance(int titleID, 
			int buttonOneTitleID, int buttonTwoTitleID, String dialogMessage)
	{
		MyAlertDialogFragment frag = new MyAlertDialogFragment(); 
		
		Bundle args = new Bundle();
		
		//There should always be a title, so add the title String Resource ID
		args.putInt(TITLE_KEY, titleID);
		
		//One button is required, so add the resource ID
		args.putInt(BUTTON_ONE_KEY, buttonOneTitleID);
		
		//The second button is not required, so make sure it exists before adding it
		if(buttonTwoTitleID != NO_BUTTON){
			args.putInt(BUTTON_TWO_KEY, buttonTwoTitleID);
		}
		
		//the message is required, so add the resource ID
		args.putString(DIALOG_MESSAGE_KEY, dialogMessage);
		
		frag.setArguments(args);
		
		return frag;
	}
	
	/*
	 * (non-Javadoc)
	 * @see android.support.v4.app.DialogFragment#onCreateDialog(android.os.Bundle)
	 */
	public Dialog onCreateDialog(Bundle SavedInstance)
	{

        //defensive coding here to ensure that the title is a valid resource
        String title;
        try
        {
            title = getActivity().getResources().getString(getArguments().getInt(TITLE_KEY));
        }
        catch(Resources.NotFoundException e)
        {
            title = DEFAULT_TITLE;
        }

        //make sure that the resource is valid, otherwise use a default title for the button
        String positiveButtonTitle;
        try
        {
            positiveButtonTitle = getActivity().getResources().getString(getArguments().getInt(BUTTON_ONE_KEY));
        }
        catch(Resources.NotFoundException e)
        {
            positiveButtonTitle = DEFAULT_TITLE_BUTTON_ONE;
        }

		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity())
                .setTitle(title)
                .setMessage(getArguments().getString(DIALOG_MESSAGE_KEY));
		

		
		alertDialogBuilder.setPositiveButton( positiveButtonTitle, new DialogInterface.OnClickListener()
            {
                public void onClick(DialogInterface dialog, int which)
                {
                    //alert the main activity to this
                    ((FragmentAlertDialog) getActivity()).alertDialogButtonClick( AlertDialog.BUTTON_POSITIVE );
                }
            });
		
		//The second button on an alert dialog is not required so only add it if needed
		int buttonTwo = getArguments().getInt(BUTTON_TWO_KEY);
		if(buttonTwo != NO_BUTTON)
        {
            String negativeButtonTitle;
			//make sure that the resource is valid, otherwise use a default title for the button
			try
            {
                negativeButtonTitle = getActivity().getResources().getString(buttonTwo);
			}
            catch(Resources.NotFoundException e)
            {
                negativeButtonTitle = DEFAULT_TITLE_BUTTON_TWO;
			}

			alertDialogBuilder.setNegativeButton(negativeButtonTitle, new DialogInterface.OnClickListener() {
				
				public void onClick(DialogInterface dialog, int which)
                {
					//alert the main activity to this
					((FragmentAlertDialog)getActivity()).alertDialogButtonClick( AlertDialog.BUTTON_NEGATIVE );
				}
			});
		}

        alertDialogBuilder.setCancelable( false );
		
		return alertDialogBuilder.create();
		
	}


	
	
}
