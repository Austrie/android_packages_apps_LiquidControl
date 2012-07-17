package com.esoftware.dev;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class Wiki extends SherlockListFragment{
        Button next, back;
	ListView list;
	int counter;
	int p;
	//Menu caller.
	@Override
	public boolean onCreateOptionsMenu(Menu menu, MenuInflater inflater) { 
 		super.onCreateOptionsMenu(menu, inflater);
		MenuInflater inflaters = getSupportMenuInflater();
		inflaters.inflate(R.menu.action_menu, menu);
		return true;
	}
	//Menu Actions
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.action_help:
			Intent Help = new Intent(Wiki.this, Help.class);
			startActivity(Help);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	// Do something when a list item is clicked
	@Override 
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		setContentView(R.layout.wikinfo);
		//Gets a list of all items in raw folder then, it opens the text file.
		InputStream in = getResources().openRawResource(R.raw.wiki);
		InputStream in2 = getResources().openRawResource(R.raw.wikinfo);
		//Making a variable out of the text file variable above to be used in BufferedReader.
		InputStreamReader input = new InputStreamReader(in);
		InputStreamReader input2 = new InputStreamReader(in2);
		//Make a BufferedReader variable out of the variable above.
		BufferedReader br = new BufferedReader(input);
		BufferedReader br2 = new BufferedReader(input2);
		TextView wikiinfoheader = (TextView)findViewById(R.id.wInfoH);
		TextView wikiinfocontent = (TextView)findViewById(R.id.wInfoC);
		try{
			if(position == 0){
				br.readLine();
				wikiinfoheader.setText(br.readLine());
				br2.readLine();
				wikiinfocontent.setText(br2.readLine());
			}else{
				int i = 0;
				for(int c = position + 1; i < c; i++){
					br.readLine();
					wikiinfoheader.setText(br.readLine());
					br2.readLine();
					wikiinfocontent.setText(br2.readLine());
				}
				p = position;
			}
			br.close();
			br2.close();
			next = (Button)findViewById(R.id.nex);
			back = (Button)findViewById(R.id.pre);
			next.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					//Gets a list of all items in raw folder then, it opens the text file.
					InputStream in = getResources().openRawResource(R.raw.wiki);
					InputStream in2 = getResources().openRawResource(R.raw.wikinfo);
					//Making a variable out of the text file variable above to be used in BufferedReader.
					InputStreamReader input = new InputStreamReader(in);
					InputStreamReader input2 = new InputStreamReader(in2);
					//Make a BufferedReader variable out of the variable above.
					BufferedReader br = new BufferedReader(input);
					BufferedReader br2 = new BufferedReader(input2);
					TextView wikiinfoheader = (TextView)findViewById(R.id.wInfoH);
					TextView wikiinfocontent = (TextView)findViewById(R.id.wInfoC);
					int i = 0;
					try{
						for(int c = p + 1; i < c; i++){
							br.readLine();
							wikiinfoheader.setText(br.readLine());
							br2.readLine();
							wikiinfocontent.setText(br2.readLine());
						}
					}catch (Exception e){

					}
				}
			});
			back.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					//Gets a list of all items in raw folder then, it opens the text file.
					InputStream in = getResources().openRawResource(R.raw.wiki);
					InputStream in2 = getResources().openRawResource(R.raw.wikinfo);
					//Making a variable out of the text file variable above to be used in BufferedReader.
					InputStreamReader input = new InputStreamReader(in);
					InputStreamReader input2 = new InputStreamReader(in2);
					//Make a BufferedReader variable out of the variable above.
					BufferedReader br = new BufferedReader(input);
					BufferedReader br2 = new BufferedReader(input2);
					TextView wikiinfoheader = (TextView)findViewById(R.id.wInfoH);
					TextView wikiinfocontent = (TextView)findViewById(R.id.wInfoC);
					
						int i = 0;
						try{
							for(int c = p - 1; i < c; i++){
								br.readLine();
								wikiinfoheader.setText(br.readLine());
								br2.readLine();
								wikiinfocontent.setText(br2.readLine());
							}
						}catch (Exception e){

						}
					}
			});
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.wikibutton);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		Settings.System.putInt(getContentResolver(), Settings.System.HAPTIC_FEEDBACK_ENABLED, 1);
		list = (ListView)findViewById(android.R.id.list);
		try{
			//			Gets a list of all items in raw folder then, it opens the text file.
			InputStream in = getResources().openRawResource(R.raw.wiki);
			//			Making a variable out of the text file variable above to be used in BufferedReader.
			InputStreamReader input = new InputStreamReader(in);
			//			Make a BufferedReader variable out of the variable above.
			BufferedReader br = new BufferedReader(input);
			ArrayList<String> people = new ArrayList<String>();
			while(br.readLine() != null){
				people.add(br.readLine().toString());
			}
			Collections.sort(people);
			ArrayAdapter<String> adapter =
					new ArrayAdapter<String>(this, R.layout.wikibutton_row, R.id.text1, people);
			list.setAdapter(adapter);
			br.close();

		}catch(Exception e){
			e.printStackTrace();
		}
	}
}


//	//Controlling Navigation Buttons
//	@Override
//	public boolean onKeyDown(int keyCode, KeyEvent event) 
//	{
//		if (keyCode == KeyEvent.KEYCODE_MENU ) {
//			if(counter == 0){
//				getWindow().setFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN,
//						WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
//			counter++;
//			return true;
//		}else{
//			getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
//					WindowManager.LayoutParams.FLAG_FULLSCREEN);
//			counter = 0;
//			return true;
//		}
//		}else{
//		return super.onKeyDown(keyCode, event);    
//	}
//	}

//		@Override
//		protected void onListItemClick(ListView l, View v, int posistion, long id){
//			super.onListItemClick(l, v, posistion, id);
//			Intent i = new Intent(this , Rem)
//		}
