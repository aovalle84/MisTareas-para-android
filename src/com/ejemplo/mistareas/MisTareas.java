package com.ejemplo.mistareas;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.speech.RecognizerIntent;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

public class MisTareas extends Activity {

	// Opciones de menú contextual
	private static final int OPCION_FINALIZAR = Menu.FIRST;
	private static final int OPCION_FINALIZAR_LLAMAR = OPCION_FINALIZAR + 1;
	private static final int OPCION_ASOCIAR_CONTACTO = 
		OPCION_FINALIZAR_LLAMAR + 1;

    // Códigos de solicitud utilizados al iniciar una Activity
    private static final int SOLICITUD_SELECCIONAR_CONTACTO = 1;
    private static final int SOLICITUD_RECONOCIMIENTO_VOZ = 2;
    
	// Referencias a controles visuales ("Widgets")
	private ListView mVistaListaTareas;
	private EditText mVistaNuevaTarea;
	private Button mBotonIniciarReconocimiento;
	
	/** Para agregar, consultar y quitar tareas .*/
	private AdminTarea mAdminTarea;
	
	/** Adaptador para vincular el arreglo de tareas con la ListView. */
	private ArrayAdapter<Tarea> mAdaptador;
	
	/** Para utilizar cuando se selecciona una tarea por medio del 
	 * menú contextual. 
	 */
	private int mIdTareaSeleccionada;
	private static final int NINGUNA_TAREA = -1;
	
	/** Este método se llamará cuando esta Activity sea creada. 
	 */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        // Obtener referencias a los controles visuales ("Widgets")
        mVistaListaTareas = (ListView) findViewById(R.id.lstTarea);
        mVistaNuevaTarea = (EditText) findViewById(R.id.txtNuevaTarea);
        mBotonIniciarReconocimiento = (Button) findViewById(R.id.btnIniciarReconocimientoVoz);
        
        mAdminTarea = new AdminTarea();
        
        // Vincular el arregloTareas con la ListView
        mAdaptador = new ArrayAdapter<Tarea>(this, 
        		android.R.layout.simple_list_item_1, 
        		mAdminTarea.getArray());
        mVistaListaTareas.setAdapter(mAdaptador);
        
        // Estar pendiente de cuando se presione la tecla central del D-Pad
        mVistaNuevaTarea.setOnKeyListener(new OnKeyListener() {
			@Override
			public boolean onKey(View unaVista, int keyCode, KeyEvent event) {
				if (event.getAction() == KeyEvent.ACTION_DOWN) {
					if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
						String nombreTarea = mVistaNuevaTarea.getText().toString();
						mAdminTarea.agregar(nombreTarea);
						mAdaptador.notifyDataSetChanged();
        				mVistaNuevaTarea.setText("");
        				return true;
					}
				}
				return false;
			}
        });
        
		// Revisar si existe una Actividad registrada para el reconocimiento de voz
        if (existeActivityReconocimiento()) {
        	// Estar pendiente del evento clic
            mBotonIniciarReconocimiento.setOnClickListener(new OnClickListener() {
    			@Override
    			public void onClick(View unaVista) {
    				iniciarActivityReconocimiento();
    			}
            });
        } else {
        	mBotonIniciarReconocimiento.setEnabled(false);
        }

        // Registrar a la ListView para menú contextual
        registerForContextMenu(mVistaListaTareas);
        
        mIdTareaSeleccionada = NINGUNA_TAREA;
    }
    
	/** Devuelve true si existe una Actividad registrada para el 
	 * reconocimiento de voz.
	 */
	private boolean existeActivityReconocimiento() {
		boolean existe = false;
		PackageManager pm = getPackageManager();
		Intent intentoReconocimiento = new Intent(
				RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		List<ResolveInfo> activities = pm.queryIntentActivities(
				intentoReconocimiento, 0);
		if (activities.size() > 0) {
			existe = true;
		}
		return existe;
	}
	
    /** Este método se llama cuando se crea el menú contextual. Se agregan
     * las opciones para finalizar tarea, finalizar y llamar por teléfono,
     * y la opción de asociar contacto a una tarea.
     */
    @Override
    public void onCreateContextMenu(ContextMenu menu,
    		View v,
    		ContextMenu.ContextMenuInfo menuInfo) {
    	super.onCreateContextMenu(menu, v, menuInfo);

    	menu.setHeaderTitle("Menú");
    	menu.add(Menu.NONE, OPCION_FINALIZAR, 0,  R.string.strOpcionFinalizar);
    	menu.add(Menu.NONE, OPCION_FINALIZAR_LLAMAR, 1, R.string.strOpcionFinalizarLlamar);
    	menu.add(Menu.NONE, OPCION_ASOCIAR_CONTACTO, 2, R.string.strOpcionAsociarContacto);
    }

    /** Este método se llama cuando se seleccione una opción del menú 
     * contextual.
     */
    @Override
    public boolean onContextItemSelected(MenuItem item) {
		super.onContextItemSelected(item);
		
		boolean comandoProcesado = false;
		AdapterView.AdapterContextMenuInfo menuInfo;
		menuInfo = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
		switch (item.getItemId()) {
			case OPCION_FINALIZAR:
				quitarTarea(menuInfo.position);
				comandoProcesado = true;
				break;
			case OPCION_FINALIZAR_LLAMAR:
				marcarTelefonoContacto(menuInfo.position);
				quitarTarea(menuInfo.position);
				comandoProcesado = true;
				break;
			case OPCION_ASOCIAR_CONTACTO:
				seleccionarContacto(menuInfo.position);
				comandoProcesado = true;
				break;
		}
		return comandoProcesado;
	}

    private void quitarTarea(int index) {
    	mAdminTarea.quitar(index);
        mAdaptador.notifyDataSetChanged();
	}
    
    /** Se inicia una Activity para marcar un número de teléfono, si el 
     * contacto seleccionado tiene número de teléfono; en caso contrario,
     * inicia una Activity que muestra los datos del contacto.
     * 
     * @param index Índice del elemento seleccionado en la ListView
     */
    private void marcarTelefonoContacto(int index) {
    	String numeroTelefono = mAdminTarea.obtenerContactoTelefono(index);
    	String strUri;
    	if (numeroTelefono.length() > 0) {
    		// Mostrar pantalla para llamar por teléfono al contacto
        	strUri = "tel:" + numeroTelefono;
        	Intent intentoMarcarTelefono = new Intent(Intent.ACTION_DIAL, 
        			Uri.parse(strUri));
        	try {
        		startActivity(intentoMarcarTelefono);
        	} catch (Exception laExcepcion) {
        		notificar("Error, no se pudo mostrar pantalla para llamar: " + 
        				laExcepcion.getMessage());
        	}
    	} else {
    		// No tienen número, entonces mostrar pantalla del contacto
        	strUri = mAdminTarea.obtenerContactoURI(index);
        	Intent intentoVerContacto = new Intent(Intent.ACTION_VIEW, 
        			Uri.parse(strUri));
        	try {
        		startActivity(intentoVerContacto);
        	} catch (Exception laExcepcion) {
        		notificar("Error, no se pudo mostrar pantalla de contacto: " + 
        				laExcepcion.getMessage());
        	}
    	}
    }
    
    /** Se inicia una Activity para seleccionar a un contacto. Se indica (en 
     * el Intent) que el tipo de contenido serán los Contactos. 
     * 
     * @param index Índice del elemento seleccionado en la ListView.
     */
    private void seleccionarContacto(int index) {
		mIdTareaSeleccionada = index;
    	Intent intentoSeleccion = new Intent(Intent.ACTION_PICK);
    	intentoSeleccion.setType(ContactsContract.Contacts.CONTENT_TYPE);
    	try {
    		startActivityForResult(intentoSeleccion, 
    				SOLICITUD_SELECCIONAR_CONTACTO);
    	} catch (Exception laExcepcion) {
    		notificar("Error, no se pudo mostrar pantalla de contactos: " + 
    				laExcepcion.getMessage());
    	}
    }
    
    /** Se inicia una Activity de Reconocimiento de voz. Se indica (en el 
     * Intent) que se desea la modalidad de "Free Form" para el reconocimiento 
     * de voz y también se especifica como descripción de la pantalla: "¿Cuál  
     * es tu tarea?".
     */
	private void iniciarActivityReconocimiento() {
		Intent intentoReconocimiento = new Intent(
				RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		intentoReconocimiento.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
				RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
		intentoReconocimiento.putExtra(RecognizerIntent.EXTRA_PROMPT, 
				"¿Cuál es tu tarea?");
		startActivityForResult(intentoReconocimiento, 
				SOLICITUD_RECONOCIMIENTO_VOZ);
	}
    
	/** Este método será llamado cuando se "devuelva el control" a esta 
	 * Activity, después de haber iniciado otra Activity.
	 * Se verifica si la Activity que terminó fue la de Selección de 
	 * contacto o si fue la de Reconocimiento de voz. También se verifica
	 * el código de resultado devuelto por dicha Activity.
	 */
    @Override
    public void onActivityResult(int codigoSolicitud, int codigoResultado, 
    		Intent data) {
    	super.onActivityResult(codigoSolicitud, codigoResultado, data);
    	
    	switch (codigoSolicitud) {
    	case SOLICITUD_SELECCIONAR_CONTACTO:
    		switch (codigoResultado) {
    		case RESULT_OK:
        		obtenerContactoSeleccionado(data);
    			break;
    		case RESULT_CANCELED:
    			notificar("No se asoció ningún contacto");
    			break;
    		}
    		break;
    	case SOLICITUD_RECONOCIMIENTO_VOZ:
    		switch (codigoResultado) {
    		case RESULT_OK:
        		obtenerTextoReconocido(data);
    			break;
    		case RESULT_CANCELED:
    			notificar("No se pudo reconocer la voz");
    			break;
    		}
    		break;
    	}
    }
    
    /** Se obtiene el contacto seleccionado (URI) y se consultan más datos
     * (nombre y teléfono) del contacto elegido.
     * 
     * @param data Datos guardados por la Activity de Selección de contacto.
     */
    private void obtenerContactoSeleccionado(Intent data) {
    	try {
    		Cursor cursor =  managedQuery(data.getData(), null, null, null, 
    				null);
    		cursor.moveToNext();
    		String contactId = cursor.getString(cursor.getColumnIndex(
    				ContactsContract.Contacts._ID));
    		String nombre = cursor.getString(cursor.getColumnIndexOrThrow(
    				ContactsContract.Contacts.DISPLAY_NAME));
    		String telefono = "";
    		
    		// Consultar el teléfono si tiene
    		int tieneTelefono = cursor.getInt(cursor.getColumnIndexOrThrow(
    				ContactsContract.Contacts.HAS_PHONE_NUMBER));
    		if (tieneTelefono == 1) {
    			try {
    				Cursor phones = getContentResolver().query(
    						ContactsContract.CommonDataKinds.Phone.CONTENT_URI, 
    						null, 
    						ContactsContract.CommonDataKinds.Phone.CONTACT_ID + 
    						" = " + contactId, 
    						null, null);
    		        if (phones.moveToNext()) 
    		        {
    		          telefono = phones.getString(phones.getColumnIndex(
    		        		  ContactsContract.CommonDataKinds.Phone.NUMBER));
    		        }
    		        phones.close();
    			} catch (Exception laExcepcion) {
    				notificar("Error, no se puedo consultar el teléfono: " + 
    						laExcepcion.getMessage());
    			}
            }
    		
    		cursor.close();
    		
    		if (mIdTareaSeleccionada != NINGUNA_TAREA) {
    			mAdminTarea.asociarContacto(mIdTareaSeleccionada, data.getData().toString(), telefono);
        		notificar("Se asoció a:  " + nombre); 
    		}
    	} catch (Exception laExcepcion) {
			notificar("Error, no se puedo consultar el contacto: " + 
					laExcepcion.getMessage());
    	}
    }
    
    /** Se obtiene el primer texto reconocido por la Activity de 
     * Reconocimiento de voz.
     * 
     * @param data Datos guardados por la Activity de Reconocimiento de voz. 
     */
    private void obtenerTextoReconocido(Intent data) {
        ArrayList<String> listaTextoReconocido = data.getStringArrayListExtra(
                RecognizerIntent.EXTRA_RESULTS);
        if (listaTextoReconocido.size() > 0) {
        	mVistaNuevaTarea.setText(listaTextoReconocido.get(0));
        }
    }
    
    private void notificar(String texto) {
    	Toast.makeText(this, texto, Toast.LENGTH_LONG).show();
    }
}
