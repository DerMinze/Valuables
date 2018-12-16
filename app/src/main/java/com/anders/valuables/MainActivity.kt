package com.anders.valuables

import android.app.Activity
import android.content.Intent
import android.graphics.*
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.util.Base64
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.*

class MainActivity : AppCompatActivity() {

    private val mValuables  : ArrayList<ValuableItem>   = ArrayList()
    private var adapter     : ValuableAdapter?          = null
    private var recyclerView: RecyclerView?             = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val fab: FloatingActionButton = findViewById(R.id.addButton)
        fab.setOnClickListener { addValuable() }

        recyclerView = findViewById(R.id.valuables_list)
        recyclerView!!.setHasFixedSize(true)
        recyclerView!!.layoutManager = LinearLayoutManager(this)
        adapter = ValuableAdapter(this, mValuables)
        recyclerView!!.adapter = adapter

        addSwipe()
        loadValuables()
    }

    private fun addValuable() {
        val addValuableIntent = Intent(this, AddValuableActivity::class.java)
        startActivityForResult(addValuableIntent, 1)
    }

    private fun editValuable() {
        val addValuableIntent = Intent(this, AddValuableActivity::class.java)
        startActivityForResult(addValuableIntent, 1)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {
                val extras = data.extras
                val bitmap = BitmapFactory.decodeByteArray(extras.getByteArray("image"), 0, extras.getByteArray("image").size)

                mValuables.add(ValuableItem(
                    extras.getString("name"),
                    extras.getString("description"),
                    extras.getString("price").toInt(),
                    bitmap))

                valuables_list.adapter.notifyDataSetChanged()

                saveValuables()
            }
        }
    }

    fun addSwipe() {
        val itemTouchHelperCallback = object:  ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView?,
                viewHolder: RecyclerView.ViewHolder?,
                target: RecyclerView.ViewHolder?
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                if (direction == ItemTouchHelper.RIGHT) {
                    adapter!!.removeItem(position)
                    saveValuables()
                }
            }

            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                val icon: Bitmap
                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                    val itemView    = viewHolder.itemView
                    val height      = itemView.bottom.toFloat() - itemView.top.toFloat()
                    val width       = height / 3
                    val p           = Paint()

                    p.color = Color.parseColor("#D32F2F")
                    val background = RectF(itemView.left.toFloat(), itemView.top.toFloat(), dX, itemView.bottom.toFloat())
                    c.drawRect(background, p)
                    icon = BitmapFactory.decodeResource(resources, R.drawable.ic_delete_white)
                    val icon_dest = RectF(itemView.left.toFloat() + width, itemView.top.toFloat() + width, itemView.left.toFloat() + 2 * width, itemView.bottom.toFloat() - width)
                    c.drawBitmap(icon, null, icon_dest, p)
                }
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            }
        }

        val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    fun createJSONObject(item: ValuableItem): JSONObject {
        val obj = JSONObject()
        obj.put("name", item.name)
        obj.put("description", item.description)
        obj.put("price", item.price)
        obj.put("image", getStringFromBitmap(item.image))
        return obj
    }

    private fun saveValuables() {
        // Save valuables list
        val jsonArray = JSONArray()
        for (valuable in mValuables) {
            val jsonObject: JSONObject = createJSONObject(valuable)
            jsonArray.put(jsonObject)
        }
        try {
            val file = File(this.filesDir, "valuables.json")
            val output: Writer = BufferedWriter(FileWriter(file))
            val jsonObject = JSONObject()
            jsonObject.put("valuables", jsonArray)
            output.write(jsonObject.toString())
            output.close()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Failed to save", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadValuables() {
        // Load valuables list
        val file = File(this.filesDir, "valuables.json")
        var text: String? = null
        var br: BufferedReader? = null

        try {
            br = BufferedReader(FileReader(file))

            text = br.use { it.readText() }
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            try {
                br?.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        try {
            val json = JSONObject(text)
            val jsonArray = json.getJSONArray("valuables")

            for (i in 0..(jsonArray.length() - 1)) {
                val item: JSONObject = jsonArray.getJSONObject(i)

                mValuables.add(ValuableItem(
                    item.getString("name"),
                    item.getString("description"),
                    item.getInt("price"),
                    getBitmapFromString(item.getString("image"))))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getStringFromBitmap(bitmap: Bitmap): String {
        val compressionQuality: Int = 0
        val byteArrayBitMapStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, compressionQuality, byteArrayBitMapStream)
        val b = byteArrayBitMapStream.toByteArray()
        val encodedImage: String = Base64.encodeToString(b, Base64.DEFAULT)
        return  encodedImage
    }

    private fun getBitmapFromString(jsonString: String): Bitmap {
        val decodedString = Base64.decode(jsonString, Base64.DEFAULT)
        val decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
        return decodedByte
    }
}
