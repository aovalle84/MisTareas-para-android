package com.ejemplo.mistareas;

import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnKeyListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

public class MisTareas extends Activity {

	static final private int OPCION_QUITAR = Menu.FIRST;

	private ListView vistaListaTareas;
	private EditText vistaNuevaTarea;
	private ArrayList<String> arregloTareas;
	private ArrayAdapter<String> adaptador;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        // Obtener referencias a los controles (widgets)
        vistaListaTareas = (ListView) findViewById(R.id.lstTarea);
        vistaNuevaTarea = (EditText) findViewById(R.id.txtNuevaTarea);
        
        // Los datos de las tareas se guardarán en un ArrayList
        arregloTareas = new ArrayList<String>();
        
        // Se requiere un adaptador para vincular el arregloTareas con la ListView
        adaptador = new ArrayAdapter<String>(this, 
        		android.R.layout.simple_list_item_1, 
        		arregloTareas);
        
        // Vincular el adaptador con la ListView
        vistaListaTareas.setAdapter(adaptador);
        
        // Temporal: Llenar arreglo con datos de prueba
        //arregloTareas.add(0, "Primera tarea");
        //arregloTareas.add(0, "Segunda tarea");
        //arregloTareas.add(0, "Tercera tarea");
        //adaptador.notifyDataSetChanged();
        
        // Estar pendiente de cuando se presione la tecla central del D-Pad
        vistaNuevaTarea.setOnKeyListener(new OnKeyListener() {

			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (event.getAction() == KeyEvent.ACTION_DOWN) {
					if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
						String nombreTarea = vistaNuevaTarea.getText().toString();
						arregloTareas.add(0, nombreTarea);
						adaptador.notifyDataSetChanged();
        				vistaNuevaTarea.setText("");
        				return true;
					}
				}
				return false;
			}
        	
        });

        // Registrar a la ListView para menú contextual
        registerForContextMenu(vistaListaTareas);

    }
    
    @Override
    public void onCreateContextMenu(ContextMenu menu,
                                    View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
      super.onCreateContextMenu(menu, v, menuInfo);

      menu.setHeaderTitle("Menú");
      menu.add(0, OPCION_QUITAR, Menu.NONE,  R.string.strOpcionQuitar);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
		super.onContextItemSelected(item);
		switch (item.getItemId()) {
			case (OPCION_QUITAR): {
				AdapterView.AdapterContextMenuInfo menuInfo;
				menuInfo = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
				quitarTarea(menuInfo.position);
				return true;
			}
		}
		return false;
	}

    private void quitarTarea(int _index) {
    	arregloTareas.remove(_index);
        adaptador.notifyDataSetChanged();
	}
    
}
