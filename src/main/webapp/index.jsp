<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="uk">
<head>
    <meta charset="UTF-8">
    <title>Онлайн сервіс</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="stylesheet" href="css/style.css">
    <style>
        body, html {
            margin: 0;
            padding: 0;
            height: 100%;
            font-family: "Lato", sans-serif;
        }

        .fullscreen-header {
            background: url(https://static.vecteezy.com/system/resources/previews/034/707/570/non_2x/ai-generated-ancient-greek-temple-free-photo.jpg) center center no-repeat;
            background-size: cover;
            height: 100vh;
            position: relative;
            color: white;
        }

        .overlay {
            position: absolute;
            top: 0; left: 0; right: 0; bottom: 0;
            background-color: rgba(0, 0, 0, 0.5);
            z-index: 1;
        }

        .content-box {
            position: relative;
            z-index: 2;
            text-align: center;
            top: 50%;
            transform: translateY(-50%);
            color: #fff;
        }

        .title-box {
            display: inline-block;
            border: 5px solid #fff;
            padding: 1em 2em;
            background-color: rgba(0, 0, 0, 0.3);
            font-size: 1.5em;
            margin-bottom: 1em;
            font-family: "Oswald", sans-serif;
        }

        .subtitle {
            font-size: 1em;
            margin-bottom: 2em;
            font-family: 'Oswald', sans-serif;
        }

        .btn-container {
            display: flex;
            justify-content: center;
            gap: 20px;
        }

        .btn {
            background-color: #F8BF03;
            color: black;
            padding: 0.8em 2em;
            font-size: 1.1em;
            border: none;
            border-radius: 5px;
            cursor: pointer;
            text-decoration: none;
            font-weight: bold;
        }

        .btn:hover {
            background-color: #A39F00;
            color: white;
        }
    </style>
</head>
<body>

<div class="fullscreen-header">
    <div class="overlay"></div>
    <div class="content-box">
        <div class="title-box">
            Онлайн сервіс ведення навчального процесу в університеті
        </div>
        <div class="subtitle">by Kateryna Nalezhyta</div>
        <div class="btn-container">
            <a href="login.jsp" class="btn">Вхід</a>
            <a href="register.jsp" class="btn">Реєстрація</a>
        </div>
    </div>
</div>
</body>
</html>
