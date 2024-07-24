

<a href='https://nyasablog.com/' target='_blank'><img class='header-img' src='https://nyasa-blog-spaces.nyc3.cdn.digitaloceanspaces.com/nyasa-blog-static/nyasa_github.png' /></a>



<h1><a href="https://nyasablog.com//">NyasaBlog Android App with Jetpack Architecture</a></h1>


<p>NyasaBlog app is a native Android application that interacts with the web app  <a href="https://nyasablog.com/" target="_blank">nyasablog.com</a>.</p>
<p>Nyasablog.com was created for Malawian content creators to be used to publish Malawian related content.Software developers are free to interact with its Rest API that comes with the web app. </p><br>


<h2><strong>Features:</strong></h2>
<ul>
<li><strong>Kotlin</strong>:</li>
<li>
<strong>Coroutines</strong>:<br>
<ol>
<li>Advanced coroutine management using jobs</li>
<li>Cancelling active jobs</li>
<li>Coroutine scoping</li>
</ol>
</li>
<li>
<strong>Navigation Components</strong>:<br>
<ol>
<li>Bottom Navigation View with fragments </li>
<li>Leveraging multiple navigation graphs (this is cutting edge content)</li>
</ol>
</li>
<li>
<strong>Dagger 2</strong>:<br>
<ol>
<li>custom scopes, fragment injection, activity injection, Viewmodel injection</li>
</ol>
</li>
<li>
<strong>MVI architecture</strong>:<br>
<ol>
<li>Basically this is MVVM with some additions</li>
<li>State management</li>
<li>Building a generic BaseViewModel</li>
<li>Repository pattern (NetworkBoundResource)</li>
</ol>
</li>
<li>
<strong>Room Persistence</strong>:<br>
<ol>
<li>SQLite on Android with Room Persistence library</li>
<li>Custom queries, inserts, deletes, updates</li>
<li>Foreign Key relationships</li>
<li>Multiple database tables</li>
</ol>
</li>
<li>
<strong>Cache</strong>:<br>
<ol>
<li>Database caching (saving data from network into local cache)</li>
<li>Single source of truth principal</li>
</ol>
</li>
<li>
<strong>Retrofit 2</strong>:<br>
<ol>
<li>Handling any type of response from server (success, error, none, etc...)</li>
<li>Returning LiveData from Retrofit calls (Retrofit Call Adapter)</li>
<li>Token authentication ( Creating an account, login and logout using  <a href="https://django-auth-token.readthedocs.io/en/latest/">django-auth-token</a> )</li>
</ol>
</li>
<li>
<strong>ViewModels</strong>:<br>
<ol>
<li>Sharing a ViewModel between several fragments</li>
<li>Building a powerful generic BaseViewModel</li>
</ol>
</li>
<li>
<strong>WebViews</strong>:<br>
<ol>
<li>Interacting with the server through a webview (Javascript)</li>
</ol>
</li>
<li>
<strong>SearchView</strong>:<br>
<ol>
<li>Programmatically implement a SearchView</li>
<li>Execute search queries to network and db cache</li>
</ol>
</li>
<li>
<strong>Images</strong>:<br>
<ol>
<li>Selecting images from phone memory</li>
<li>Cropping images to a specific aspect ratio</li>
<li>Setting limitations on image size and aspect ratio</li>
<li>Uploading a cropped image to server</li>
</ol>
</li>
<li>
<strong>Network Request Management</strong>:<br>
<ol>
<li>Cancelling pending network requests (Kotlin coroutines)</li>
<li>Testing for network delays</li>
</ol>
</li>
<li>
<strong>Pagination</strong>:<br>
<ol>
<li>Paginating objects returned from server and database cache</li>
</ol>
</li>
<li>
<strong>Material Design</strong>:<br>
<ol>
<li>Bottom Navigation View with Fragments</li>
<li>Customizing Bottom Navigation Icon behavior</li>
<li>Handling Different Screen Sizes (ConstraintLayout)</li>
<li>Material Dialogs</li>
<li>Fragment transition animations</li>
</ol>
</li>
</ul>
<br>