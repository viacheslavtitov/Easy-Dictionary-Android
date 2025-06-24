package org.easy.dictionary.app.domain.models.navigation

sealed class ActionNavigation()

class AddTagNavigation(): ActionNavigation()
class AddTranslationVariantNavigation(): ActionNavigation()