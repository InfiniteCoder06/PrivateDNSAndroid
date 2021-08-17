package ru.karasevm.privatednstoggle

import android.Manifest
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import kotlinx.android.synthetic.main.activity_main.*
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri


class MainActivity : AppCompatActivity(), AddServerDialogFragment.NoticeDialogListener, DeleteServerDialogFragment.NoticeDialogListener {

    private lateinit var linearLayoutManager: LinearLayoutManager
    public var items = mutableListOf<String>()
    lateinit var sharedPrefs: SharedPreferences
    lateinit var adapter: RecyclerAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (checkSelfPermission(Manifest.permission.WRITE_SECURE_SETTINGS) != PackageManager.PERMISSION_GRANTED) {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("http://www.google.com")) //TODO: REPLACE LINK
            startActivity(browserIntent)
            finish()
        }
        linearLayoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = linearLayoutManager

        sharedPrefs = this.getSharedPreferences("app_prefs", 0);

        items = sharedPrefs.getString("dns_servers", "")!!.split(",").toMutableList()
        if (items[0] == "") {
            items.removeAt(0)
        }
        adapter = RecyclerAdapter(items)
        adapter.onItemClick = { position ->
            val newFragment = DeleteServerDialogFragment(position)
            newFragment.show(supportFragmentManager, "delete_server")
        }
        recyclerView.adapter = adapter
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.add_server -> {
            val newFragment = AddServerDialogFragment()
            newFragment.show(supportFragmentManager, "add_server")
            true
        }
        R.id.privacy_policy -> {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("http://www.google.com"))//TODO: REPLACE LINK
            startActivity(browserIntent)
            true
        }

        else -> {
            // If we got here, the user's action was not recognized.
            // Invoke the superclass to handle it.
            super.onOptionsItemSelected(item)
        }
    }

    override fun onDialogPositiveClick(dialog: DialogFragment, server: String) {
        if (server.length == 0) {
            Toast.makeText(this, R.string.server_length_error, Toast.LENGTH_SHORT).show()
            return
        }
        items.add(server)
        adapter.setData(items.toMutableList())
        recyclerView.adapter?.notifyItemInserted(items.size - 1)
        sharedPrefs.edit()
            .putString("dns_servers", items.joinToString(separator = ",") { it -> it }).commit()
    }

    override fun onDialogPositiveClick(dialog: DialogFragment,position: Int) {
        items.removeAt(position)
        adapter.setData(items.toMutableList())
//        adapter.notifyItemRangeChanged(position, items.size - position -2)
        adapter.notifyDataSetChanged() // TODO: DON'T USE THIS
        sharedPrefs.edit()
            .putString("dns_servers", items.joinToString(separator = ",") { it -> it }).commit()

    }


}