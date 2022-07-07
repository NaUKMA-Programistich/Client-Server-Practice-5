### HTTP Server
Ви маєте створити HTTP Сервер, що надає світу наступний REST API:
* /login
```
method POST
params: login, password in MD5 encoding
responses:
200 Ok and Uniq token
401 Unauthorized
Всі методи api мають викликатися з Auth токеном, що передається в хедерах запиту. 
Токен має бути перевірений на валідність. 
Якщо токен не валідний - повертати 403 помилку. 
Пропонується використати JWT:https://stormpath.com/blog/jwt-java-create-verify
```
* /api/good
```
method GET
responses: 200 Ok
errors: 404
```
* /api/good
```
method PUT
body: json with information about good that should be created
response: 201 Created with id of created good
errors: 409 Conflict (if information has wrong information, for example price of good is -9)
```
* /api/good/{id}
```
method GET
responses: 200 Ok
Body - json with information about good
errors: 404
```
* /api/good/{id}
```
method POST
body: json with information that should be changed in already existed good
response: 204 No Content
errors: 404, 409
```
* /api/good/{id}
```
method DELETE
body: empty
response: 204 No Content
Errors: 404 Not found
```