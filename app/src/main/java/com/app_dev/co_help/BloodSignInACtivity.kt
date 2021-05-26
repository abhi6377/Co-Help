package com.app_dev.co_help

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.widget.Toast
import com.app_dev.co_help.Daos.UserDao
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.core.Context
import kotlinx.android.synthetic.main.activity_blood_sign_in.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class BloodSignInACtivity : AppCompatActivity() {

    var auth=FirebaseAuth.getInstance()
    private  lateinit var googleSignInClient: GoogleSignInClient
    private val  RC_SIGN_IN:Int = 123

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_blood_sign_in)

        supportActionBar!!.title = "Blood Plasma Verification "
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("776125300491-laosr0mmekh8holf9ueh06an8do1iism.apps.googleusercontent.com")
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)
        auth = FirebaseAuth.getInstance()

        signInBtn.setOnClickListener{
            signIn()
        }

    }
//
//    override fun onStart() {
//        super.onStart()
//
//        val currentUser = auth.currentUser
//        updateUI(currentUser)
//    }

    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
//            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val task:Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }
    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>){
        try {
            val account: GoogleSignInAccount? =completedTask.getResult(ApiException::class.java)
            if (account != null) {
                UpdateUI(account)
            }
        } catch (e:ApiException){
            Toast.makeText(this,e.toString(),Toast.LENGTH_SHORT).show()
        }
    }
    private fun UpdateUI(account: GoogleSignInAccount){
        val credential= GoogleAuthProvider.getCredential(account.idToken,null)

        val firebaseUser = auth.currentUser

        auth.signInWithCredential(credential).addOnCompleteListener {task->
            if(task.isSuccessful) {

                if(firebaseUser!=null) {
                    val user = com.app_dev.co_help.Models.User(
                        firebaseUser.uid,
                        firebaseUser.displayName.toString(),
                        firebaseUser.photoUrl.toString()
                    )
                    val usersDao = UserDao()
                    usersDao.addUser(user)

                    val intent = Intent(this, MainFeedActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }
        }
    }
    override fun onStart() {
        super.onStart()
        if(GoogleSignIn.getLastSignedInAccount(this)!=null){
            startActivity(Intent(this, MainFeedActivity::class.java))
            finish()
        }
    }


//    private fun handleSignInResult(task: Task<GoogleSignInAccount>?) {
//
//        try {
//            val account = task?.getResult(ApiException::class.java)!!
//
//            Log.d(ContentValues.TAG,"firebaseAuthWithGoogle"+account.id)
//            firebaseAuthWithGoogle(account.idToken!!)
//
//        }catch (e: ApiException){
//            Log.w(ContentValues.TAG,"signInResult:Failed code" + e.statusCode)
//        }
//    }
//
//    private fun firebaseAuthWithGoogle(idToken: String) {
//
//        val credential = GoogleAuthProvider.getCredential(idToken,null)
//
//        GlobalScope.launch(Dispatchers.IO) {
//            runOnUiThread{
//                signInBtn.visibility = View.GONE
//                progressbar.visibility = View . VISIBLE
//            }
//
//            val auth = auth.signInWithCredential(credential).await()
//            val firebaseUser = auth.user
//            withContext(Dispatchers.Main){
//                updateUI(firebaseUser)
//            }
//        }
//
//    }
//
//    @SuppressLint("RestrictedApi")
//    private fun updateUI(firebaseUser: FirebaseUser?) {
//        if(firebaseUser!=null){
//
//            val user = com.app_dev.co_help.Models.User(firebaseUser.uid, firebaseUser.displayName , firebaseUser.photoUrl.toString())
//            val usersDao = UserDao()
//            usersDao.addUser(user)
//
//            val intent = Intent(this,MainFeedActivity::class.java)
//            startActivity(intent)
//            finish()
//        }else{
//            runOnUiThread {
//
//                signInBtn.visibility = View.VISIBLE
//                progressbar.visibility = View.GONE
//            }
//
//        }
//    }

    object SavedPreference {

        const val EMAIL= "email"
        const val USERNAME="username"

        private  fun getSharedPreference(ctx: android.content.Context): SharedPreferences? {
            return PreferenceManager.getDefaultSharedPreferences(ctx)
        }

        private fun  editor(context: android.content.Context, const:String, string: String){
            getSharedPreference(
                context
            )?.edit()?.putString(const,string)?.apply()
        }

        fun getEmail(context: android.content.Context)= getSharedPreference(
            context
        )?.getString(EMAIL,"")

        fun setEmail(context: android.content.Context, email: String){
            editor(
                context,
                EMAIL,
                email
            )
        }

        fun setUsername(context: android.content.Context, username:String){
            editor(
                context,
                USERNAME,
                username
            )
        }

        fun getUsername(context: android.content.Context) = getSharedPreference(
            context
        )?.getString(USERNAME,"")

    }
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

}

