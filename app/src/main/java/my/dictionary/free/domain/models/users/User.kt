package my.dictionary.free.domain.models.users

data class User(
    val _id: String? = null,
    val name: String,
    val email: String,
    val providerId: String,
    val uid: String
) {
    override fun toString(): String {
        return "_id=$_id | name=$name | email=$email | providerId=$providerId | uid=$uid"
    }

    override fun equals(other: Any?): Boolean {
        return other != null && other is User && _id == other._id && name == other.name && email == other.email && providerId == other.providerId && uid == other.uid
    }
}