package com.bignerdranch.android.z2.ui.home

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.asLiveData
import com.bignerdranch.android.z2.ui.database.MyData
import com.bignerdranch.android.z2.ui.database.MyDataBase
import com.example.android.z2.databinding.FragmentHomeBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var ivMyImage: ImageView
    private lateinit var imageUrl: Uri
    private lateinit var db: MyDataBase
    private lateinit var name: EditText
    private lateinit var surname: EditText
    private lateinit var group: EditText
    private lateinit var buttonAccept: Button

    private val requestCameraPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            launchCamera()
        } else {
            Toast.makeText(requireContext(), "Доступ к камере отклонен", Toast.LENGTH_SHORT).show()
        }
    }

    private val contract = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            ivMyImage.setImageURI(imageUrl)
        } else {
            Toast.makeText(requireContext(), "Не удалось сделать фото", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        imageUrl = createImageUri()
        ivMyImage = binding.imageView3
        name = binding.nameText
        surname = binding.surnameText
        group = binding.groupText
        buttonAccept = binding.button2

        ivMyImage.setOnClickListener {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                launchCamera()
            } else {
                requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }

        db = MyDataBase.getInstance(requireContext())

        loadDataFromDatabase()

        buttonAccept.setOnClickListener {
            saveDataToDatabase()
        }

        return root
    }

    private fun loadDataFromDatabase() {
        db.getDbDao().query().asLiveData().observe(viewLifecycleOwner) { dataList ->
            if (dataList.isNotEmpty()) {
                val data = dataList[0]
                Log.d("HomeFragment", "дата лист загружен: $data")
                updateUI(data)
            } else {
                Log.d("HomeFragment", "дата лист пустой")
            }
        }
    }

    private fun updateUI(data: MyData) {
        name.setText(data.name)
        surname.setText(data.surname)
        group.setText(data.group)
        val bitmap = data.image?.let { BitmapFactory.decodeByteArray(data.image, 0, it.size) }
        ivMyImage.setImageBitmap(bitmap)
    }

    private fun saveDataToDatabase() {
        val name = name.text.toString()
        val surname = surname.text.toString()
        val group = group.text.toString()
        val imageBytes = convertImageToBytes(ivMyImage)

        if (name.isEmpty() || surname.isEmpty() || group.isEmpty() || imageBytes == null) {
            Log.e("HomeFragment", "Все поля должны быть заполнены и фотография должна быть сделана.")
            return
        }

        Log.d("HomeFragment", "Длина изображения в байтах: ${imageBytes.size}")

        val data = MyData(
            PrimaryKey = 1, // Обратите внимание на значение PrimaryKey
            image = imageBytes,
            name = name,
            surname = surname,
            group = group
        )

        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    db.getDbDao().insert(data) // Используйте insert вместо update
                    Log.d("HomeFragment", "Данные успешно сохранены: $data")
                } catch (e: Exception) {
                    Log.e("HomeFragment", "Ошибка сохранения данных", e)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun createImageUri(): Uri {
        val image = File(requireActivity().filesDir, "myPhoto.png")
        return FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().applicationInfo.packageName}.fileprovider",
            image
        )
    }

    private fun convertImageToBytes(imageView: ImageView): ByteArray? {
        try {
            val drawable = imageView.drawable
            if (drawable is BitmapDrawable) {
                val bitmap = drawable.bitmap

                val outputStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
                val compressedBitmap = BitmapFactory.decodeStream(ByteArrayInputStream(outputStream.toByteArray()))

                val compressedStream = ByteArrayOutputStream()
                compressedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, compressedStream)
                return compressedStream.toByteArray()
            }
        } catch (e: OutOfMemoryError) {
            Log.e("HomeFragment", "Ошибка нехватки памяти при преобразовании изображения в байты", e)
        } catch (e: Exception) {
            Log.e("HomeFragment", "Ошибка преобразования изображения в байты.", e)
        }
        return null
    }


    private fun launchCamera() {
        contract.launch(imageUrl)
    }
}
