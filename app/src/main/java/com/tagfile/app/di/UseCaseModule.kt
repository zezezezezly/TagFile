package com.tagfile.app.di

// UseCaseModule has been removed.
// All use cases have @Inject constructors and are provided by Hilt automatically.
// The previous @Provides methods created dependency cycles because they
// took an instance as a parameter and returned the same instance.
