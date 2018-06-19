package com.whitecloud.hm.whiteclouduser.old;

import android.app.Activity;
import android.content.DialogInterface;
import android.graphics.Color;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AlertDialog;
import android.text.InputFilter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.whitecloud.hm.whiteclouduser.MainActivity;


import org.json.JSONException;
import org.json.JSONObject;

import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;

public class callAlertBox extends Activity {

	private static Activity activity ;
	
	public static Activity getActivity() {
	    return activity;
	}

    public static callAlertBox_interface listener=null;

    public interface callAlertBox_interface {
        void callAlertBox_interface(JSONObject jObj);
    }

	public void setActivity(Activity mactivity, callAlertBox_interface listen) {
	    activity = mactivity;
        this.listener = listen;
	}

	public static void showMyAlertBox(String id){

        if(MainActivity.dlg!=null)
            MainActivity.dlg.dismiss();

		AlertDialog.Builder ad = new AlertDialog.Builder(activity);
		ad.setCancelable(true); // This blocks the 'BACK' button if false
		//ad.setMessage(id);
		final ScrollView s_view = new ScrollView(activity);
		final TextView t_view = new TextView(activity);
		t_view.setPadding(30, 5, 20, 10);
		t_view.setTextColor(Color.BLACK);
		t_view.setBackgroundColor(Color.WHITE);
		t_view.setTextSize(MainActivity.v_tamanoFuente);
		s_view.addView(t_view);
		t_view.setText(id);
		ad.setView(s_view);

		ad.setNegativeButton("Cerrar", new DialogInterface.OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int id){
				if(MainActivity.vibrar!=null)
					MainActivity.vibrar.cancel();
				if(MainActivity.mpSplash!=null){
					MainActivity.mpSplash.stop();
					MainActivity.v_sonando=false;
				}
			}
		});
		MainActivity.dlg=ad.create();
		MainActivity.dlg.show();
	}

	public static void showMyAlertBox_msg(final JSONObject jObj){

        if(MainActivity.dlg!=null)
            MainActivity.dlg.dismiss();

		try {
			AlertDialog.Builder ad = new AlertDialog.Builder(activity);
			ad.setCancelable(true); // This blocks the 'BACK' button if false
			//ad.setMessage(id);
			final ScrollView s_view = new ScrollView(activity);
			final TextView t_view = new TextView(activity);
			t_view.setPadding(30, 5, 20, 10);
			t_view.setTextColor(Color.BLACK);
			t_view.setBackgroundColor(Color.WHITE);
			t_view.setTextSize(MainActivity.v_tamanoFuente);
			s_view.addView(t_view);
			t_view.setText(jObj.getString("desc"));
			ad.setTitle(jObj.getString("origen"));
			ad.setView(s_view);
			ad.setPositiveButton("Revisar", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int id) {
					if (MainActivity.vibrar != null)
						MainActivity.vibrar.cancel();
					if (MainActivity.mpSplash != null) {
						MainActivity.mpSplash.stop();
						MainActivity.v_sonando = false;
					}
					listener.callAlertBox_interface(jObj);
				}
			});
			ad.setNegativeButton("Cerrar", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int id) {
					if (MainActivity.vibrar != null)
						MainActivity.vibrar.cancel();
					if (MainActivity.mpSplash != null) {
						MainActivity.mpSplash.stop();
						MainActivity.v_sonando = false;
					}
				}
			});
			MainActivity.dlg = ad.create();
			MainActivity.dlg.show();
		}catch (JSONException e){
			e.printStackTrace();
		}
	}

	public static void showMyAlertBoxMsg(String id){
		AlertDialog.Builder ad = new AlertDialog.Builder(activity);
	    ad.setCancelable(false); // This blocks the 'BACK' button if false
	    //ad.setMessage(id);
	    final ScrollView s_view = new ScrollView(activity);
		final TextView t_view = new TextView(activity);
		t_view.setPadding(30, 5, 20, 10);
		t_view.setTextColor(Color.BLACK);
		t_view.setBackgroundColor(Color.WHITE);
		t_view.setTextSize(MainActivity.v_tamanoFuente);
		s_view.addView(t_view);
		t_view.setText(id);
		ad.setView(s_view);

	    ad.setNegativeButton("Cerrar", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int id){
            	if(MainActivity.vibrar!=null)
					MainActivity.vibrar.cancel();
            	if(MainActivity.mpSplash!=null){
					MainActivity.mpSplash.stop();
					MainActivity.v_sonando=false;
            	}
            }
        });
		MainActivity.dlg=ad.create();
		MainActivity.dlg.show();

	    final Timer t = new Timer();
        t.schedule(new TimerTask() {
            public void run() {
            	if(MainActivity.dlg!=null)
					MainActivity.dlg.dismiss(); // when the task active then close the dialog
                t.cancel(); // also just top the timer thread, otherwise, you may receive a crash report
				MainActivity.dlg=null;
                if(MainActivity.vibrar!=null)
					MainActivity.vibrar.cancel();
            	if(MainActivity.mpSplash!=null){
					MainActivity.mpSplash.stop();
					MainActivity.v_sonando=false;
            	}
            }
        }, 1000*60*2); // only 120 seconds (or 120000 miliseconds), the task will be active.
	}

	public static void showMyAlertBoxServicio(final String cadena){
		AlertDialog.Builder ad = new AlertDialog.Builder(activity);
	    ad.setCancelable(true); // This blocks the 'BACK' button if false
	   // ad.setMessage(id);
	    final ScrollView s_view = new ScrollView(activity);
		final TextView t_view = new TextView(activity);
		t_view.setPadding(30, 5, 20, 10);
		t_view.setBackgroundColor(Color.WHITE);
		t_view.setTextColor(Color.BLACK);
		t_view.setTextSize(MainActivity.v_tamanoFuente);
		s_view.addView(t_view);
		t_view.setText(cadena);
		ad.setView(s_view);
		if(MainActivity.v_preftts){
			ad.setPositiveButton("Repetir", new DialogInterface.OnClickListener(){
	            @Override
	            public void onClick(DialogInterface dialog, int id){
	    		   		if(MainActivity.v_tts){

                            String[] tokito = MainActivity.splitTotokens(cadena,"\n");

	    		   			String nuevoMsg = tokito[0] + ", " +tokito[1];
	    		   			nuevoMsg=nuevoMsg.replaceAll(" - ", " Número ");
	    		   			nuevoMsg=nuevoMsg.replaceAll(" # ", " Número ");
	    		   			nuevoMsg=nuevoMsg.replaceAll("CL ", "Calle ");
	    		   			nuevoMsg=nuevoMsg.replaceAll("CR ", "Carrera ");
	    		   			nuevoMsg=nuevoMsg.replaceAll("TV ", "Transversal ");
	    		   			nuevoMsg=nuevoMsg.replaceAll("DG ", "Diagonal ");
	    		   			nuevoMsg=nuevoMsg.replaceAll("PJ ", "Pasaje ");
	    		   			nuevoMsg=nuevoMsg.replaceAll("CJ ", "Callejón ");
	    		   			nuevoMsg=nuevoMsg.replaceAll("AV ", "Avenida ");
	    		   			nuevoMsg=nuevoMsg.replaceAll("GEOSERVICIO", "Geo Servicio");
							MainActivity.tts.speak(nuevoMsg.toLowerCase(), TextToSpeech.QUEUE_FLUSH, null);
	    		   		}
	    		   		showMyAlertBoxServicio(cadena);
	            }
	        });
		}
	    ad.setNegativeButton("Cerrar", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int id){

            }
        });

		MainActivity.dlg=ad.create();
		MainActivity.dlg.show();

	}

	public static void showMyAlertBoxGrupal(String id){
		final CharSequence items[];
		StringTokenizer tokens = new StringTokenizer(id, ";");
		int z=tokens.countTokens();
		items = new String[z];
		for(int i=0;i<z;i++){
			items[i] = tokens.nextToken();
			System.out.println(items[i]);
		}

           //Prepare the list dialog box
           AlertDialog.Builder builder = new AlertDialog.Builder(activity);
           //Set its title
           builder.setTitle("Mensaje Grupal");
           //Set the list items and assign with the click listener
           builder.setItems(items, new DialogInterface.OnClickListener() {
               // Click listener
               public void onClick(DialogInterface dialog, int item) {

            	   if(!items[item].equals("PERSONALIZADO")){
					   //MainActivity.networktask.SendDataToNetwork("37|"+MainActivity.v_latitud+"|"+MainActivity.v_longitud+"|"+items[item]+"|");
            	   }else{
            		   	System.out.println("."+items[item]+".");

						AlertDialog.Builder alert = new AlertDialog.Builder(activity);
						alert.setTitle("Envío de Mensaje Grupal:");
						// alert.setMessage("Móvil - Mensaje");
						// Set an EditText view to get user input
						LinearLayout lila1 = new LinearLayout(activity);
						lila1.setOrientation(LinearLayout.VERTICAL);

						final EditText input1 = new EditText(activity);
						InputFilter[] FilterArray1 = new InputFilter[1];
						FilterArray1[0] = new InputFilter.LengthFilter(140);
						input1.setFilters(FilterArray1);
						input1.setHint("Mensaje (140 caracteres max.)");
						input1.setLines(3);
						lila1.addView(input1);
						alert.setView(lila1);

						alert.setPositiveButton("OK",
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int whichButton) {
										String t2 = input1.getText().toString().trim();
										//if (!t2.equals(""))
											//MainActivity.networktask.SendDataToNetwork("37|"+MainActivity.v_latitud+"|"+MainActivity.v_longitud+"|"+t2+"|");
									}
								});

						alert.setNegativeButton("Cancelar",
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int whichButton) {
										// Canceled.
									}
								});

						alert.show();

            	   }
               }
           });
           builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
     		  @Override
 			public void onClick(DialogInterface dialog, int whichButton) {
     		    // Canceled.
     		  }
     		});
           AlertDialog alert = builder.create();
           //display dialog box
           alert.show();
	}

}
