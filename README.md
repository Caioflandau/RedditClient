# Reddit Client
This app is a simple Reddit client that displays (up to) the top 50 posts from Reddit's `top` posts. The 

It serves as a demo of an MVVM architecture using Coroutines and Flow (more architecture details below)

## Features
The following features have been implemented:
- Pagination
- Pull-to-refresh
- Save pictures locally
- State preservation (provided by Android Architecture's ViewModel)
- Read/unread indicator (in-memory - for the duration of the session)
- Post filtering
- Dismiss all posts button
- Split layout (tablet sizes) and single-pane, multi-activity (phone) support
- Open post's URL on browser
- View post on Reddit.com

## Architecture
This app follows the MVVM architecture. Data flows from the ViewModel to the view, and events flow from the view to the ViewModel.

At first glance, this specific implementation of MVVM may seem complex. Trust me on this, it's actually not. We expose inputs and outputs from the ViewModel, which represent - you guessed - events from the view and data from the viewModel, respectively. The ViewModel performs all business logic needed for each input, and then outputs the necessary data for the view to render. That's it.

Here's how it works in more details:
- This app makes heavy use of [Android Architecture Components](https://developer.android.com/topic/libraries/architecture) and [Kotlin Coroutines](https://kotlinlang.org/docs/reference/coroutines-overview.html). RxJava would be the closest relative to this.
- Each ViewModel has two inner classes: an `Input` and an `Output`
- The ViewModel has one instance of each of those. Each has only one data direction: either to or from the ViewModel
- The `Input` instance is responsible for communication from `View` -> `ViewModel`. This communication is performed using [Coroutine Channels](https://kotlinlang.org/docs/reference/coroutines/channels.html)
- The `Output` instance exposes [LiveData](https://developer.android.com/topic/libraries/architecture/livedata) objects that represent UI elements to be displayed/handled by the `View`
- All business logic resides in the `ViewModel`. The `View` simply displays data and proxies events
- List pagination is done using a `PagedListAdapter`. This is part of Android's Architecture Components along with `PagedList`

## Data layer
The data layer is composed of:
- The `Api` wrapper, which wraps the required REST APIs
- A `RedditPostRepository`, which has two purposes: make API calls (and convert them to app-domain models) and provide a wrapper around list mutations needed for dismissing posts / marking them as read locally
- API response objects (`RedditPostsResponse`), which map 1:1 to Reddit's JSON
- App-domain models (which are converted from the API responses in the repository)
- The API calls are handled with [Retrofit](https://square.github.io/retrofit/) and Coroutines (Retrofit ships with support out of the box!)

## Tests
- Unit tests for all business logic have been included
- The tests are all regular unit tests. Wrappers were created around Android components to avoid needing Robolectric or similar (which are pretty slow)
- Code coverage is ~80%

## Assumptions made
- App supports the latest available Android version (API 29)
- There's a hard cap of 50 posts maximum. When paging reaches 50, it will stop loading more posts (as per assignment)

## Running the project
- **App**: Just open on Android Studio and run the `app` configuration
- **Tests**: Running the full suite is simply a matter of:
  - Right-click the top-level package that is marked with `(test)`
  - Click `Run tests in ...`