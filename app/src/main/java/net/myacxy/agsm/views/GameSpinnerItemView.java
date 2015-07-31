package net.myacxy.agsm.views;

import android.content.Context;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.myacxy.agsm.R;
import net.myacxy.jgsq.model.Game;

import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;

@EViewGroup(R.layout.item_game_spinner)
public class GameSpinnerItemView extends LinearLayout
{

    @ViewById(R.id.add_server_spinner_item_icon)
    ImageView icon;

    @ViewById(R.id.add_server_spinner_item_name)
    TextView name;

    public GameSpinnerItemView(Context context) {
        super(context);
    }

    public void bind(Game game)
    {
        name.setText(game.name);
        icon.setImageDrawable(getResources().getDrawable(R.drawable.ic_settings_black_24dp));
    }
}