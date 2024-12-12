package com.laila.sustainwise.ui.account

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.FirebaseAuth
import com.laila.sustainwise.R
import com.laila.sustainwise.data.retrofit.RetrofitInstance
import com.laila.sustainwise.ui.about.AboutActivity
import com.laila.sustainwise.ui.editprofile.EditProfileActivity
import com.laila.sustainwise.ui.faq.FaqActivity
import com.laila.sustainwise.ui.login.LoginActivity
import com.laila.sustainwise.ui.signup.UserProfileResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AccountFragment : Fragment() {
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        googleSignInClient = GoogleSignIn.getClient(
            requireContext(),
            com.google.android.gms.auth.api.signin.GoogleSignInOptions.DEFAULT_SIGN_IN
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_account, container, false)

        // Logout Button
        val cardLogout = rootView.findViewById<CardView>(R.id.cardLogout)
        cardLogout.setOnClickListener { performLogout() }

        // Edit Profile Button
        val btnEditProfile = rootView.findViewById<Button>(R.id.btnEditProfile)
        btnEditProfile.setOnClickListener {
            val intent = Intent(requireContext(), EditProfileActivity::class.java)
            startActivity(intent)
        }

        // FAQ Button
        val cardFaq = rootView.findViewById<CardView>(R.id.cardFaq)
        cardFaq.setOnClickListener {
            val intent = Intent(requireContext(), FaqActivity::class.java)
            startActivity(intent)
        }

        // About Button
        val cardAbout = rootView.findViewById<CardView>(R.id.cardAbout)
        cardAbout.setOnClickListener {
            val intent = Intent(requireContext(), AboutActivity::class.java)
            startActivity(intent)
        }

        // Load User Profile
        val user = FirebaseAuth.getInstance().currentUser
        user?.getIdToken(true)?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val idToken = task.result?.token
                idToken?.let { getUserProfile() }
            }
        }

        return rootView
    }

    private fun performLogout() {
        FirebaseAuth.getInstance().signOut()
        googleSignInClient.signOut().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(requireContext(), "Logout successful", Toast.LENGTH_SHORT).show()
                val intent = Intent(requireContext(), LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            } else {
                Toast.makeText(requireContext(), "Failed to logout", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getUserProfile() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        currentUser?.getIdToken(true)?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val idToken = task.result?.token
                if (idToken != null) {
                    RetrofitInstance.api.getUserProfile("Bearer $idToken").enqueue(object : Callback<UserProfileResponse> {
                        override fun onResponse(
                            call: Call<UserProfileResponse>,
                            response: Response<UserProfileResponse>
                        ) {
                            if (response.isSuccessful) {
                                val userProfile = response.body()
                                userProfile?.let { updateUI(it) }
                            } else {
                                Log.e("AccountFragment", "Failed to load user profile. Response code: ${response.code()}")
                            }
                        }

                        override fun onFailure(call: Call<UserProfileResponse>, t: Throwable) {
                            Log.e("AccountFragment", "Error: ${t.message}")
                        }
                    })
                } else {
                    Log.e("AccountFragment", "Failed to get ID token")
                }
            } else {
                Log.e("AccountFragment", "Error getting Firebase ID token")
            }
        }
    }

    private fun updateUI(userProfile: UserProfileResponse) {
        val tvProfileName = view?.findViewById<TextView>(R.id.tvProfileName)
        tvProfileName?.text = userProfile.username

        val ivProfilePicture = view?.findViewById<ImageView>(R.id.ivProfilePicture)
        ivProfilePicture?.let {
            Glide.with(this)
                .load(userProfile.photo)
                .placeholder(R.drawable.ic_profile_placeholder)
                .circleCrop()
                .into(it)
        }
    }
}
