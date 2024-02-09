package my.dictionary.free.domain.models.navigation

sealed class ActionNavigation()

class AddTagNavigation(): ActionNavigation()
class AddTranslationVariantNavigation(): ActionNavigation()