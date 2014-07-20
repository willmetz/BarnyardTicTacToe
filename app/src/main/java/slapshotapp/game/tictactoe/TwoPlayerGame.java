package slapshotapp.game.tictactoe;

import android.view.View;

public class TwoPlayerGame extends PlayGame
{

	@Override
	public void InitGame()
	{
	}
	
    @Override
    public void StartGame()
    {
    }
    
    @Override
	public void FocusGained()
    {
    }
    
    @Override
    public void FocusLost()
    {
    }
    
    @Override
    public void GameNotVisible()
    {
    }
    
    public void GameBoardClickListener(View target)
    {
    	//let the base class handle the move
		BoardMove(target.getId());
    }

}
