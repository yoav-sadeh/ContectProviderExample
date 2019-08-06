package com.yoavs.contentproviderexample

import android.Manifest.permission.READ_CONTACTS
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.Settings
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*

private const val TAG = "MainActivity"

private val REQUEST_CODE_READ_CONTACTS: Int = 1

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate starts")
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        verifyPermissionsRoutine()
        fab.setOnClickListener({ view -> fabClick(view) })
        Log.d(TAG, "onCreate ends")
    }

    private fun verifyPermissionsRoutine() {
        val hasReadContactPermissions = hasSelfPermissions()

        if (hasReadContactPermissions) {
            Log.d(TAG, "verifyPermissionsRoutine: permission granted")
        } else {
            requestContactPermissions()
        }
    }

    private fun hasSelfPermissions(): Boolean {
        val hasReadContactPermissions = ContextCompat.checkSelfPermission(this, READ_CONTACTS)
        Log.d(TAG, "hasSelfPermissions permission: $hasReadContactPermissions")
        return hasReadContactPermissions == PackageManager.PERMISSION_GRANTED
    }

    private fun requestContactPermissions() {
        Log.d(TAG, "onCreate: requesting permission")
        ActivityCompat.requestPermissions(this, arrayOf(READ_CONTACTS), REQUEST_CODE_READ_CONTACTS)
    }

    private fun fabClick(view: View) {
        Log.d(TAG, "fab onClick starts")
        if (hasSelfPermissions()) {
            val projections = arrayOf(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY)
            val cursor = contentResolver.query(
                ContactsContract.Contacts.CONTENT_URI,
                projections,
                null,
                null,
                ContactsContract.Contacts.DISPLAY_NAME_PRIMARY
            )
            val contacts = ArrayList<String>()

            cursor?.use {
                while (it.moveToNext()) {
                    contacts.add(it.getString(it.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY)))
                }
            }
            val adapter = ArrayAdapter<String>(this, R.layout.contact_detail, R.id.name, contacts)
            contact_names.adapter = adapter
        } else {
            Snackbar.make(view, "You denied me contact permissions ya douch!!!", Snackbar.LENGTH_INDEFINITE)
                .setAction("Grant Access Moron!", {
                    Log.d(TAG, "Snackbar click start")
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this, READ_CONTACTS)) {
                        requestContactPermissions()
                    } else {
                        Log.d(TAG, "Snackbar click: Redirecting to settings to allow contact access")
                        val intent = Intent()
                        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                        intent.data = Uri.fromParts("package", this.packageName, null)
                        startActivity(intent)
                    }
                }).show()
        }

        Log.d(TAG, "fab onClick ends")
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }
}
