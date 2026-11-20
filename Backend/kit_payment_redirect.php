<?php
// PhonePe Redirect URL Landing Page for Kit / Training Payment
$code = $_GET['code'] ?? ($_POST['code'] ?? '');
$txnId = $_GET['transactionId'] ?? ($_POST['transactionId'] ?? ($_GET['merchantTransactionId'] ?? ($_POST['merchantTransactionId'] ?? '')));
$isSuccess = ($code === 'PAYMENT_SUCCESS');
?>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Jiifit - Training & Kit Payment</title>
    <style>
        body {
            font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
            background-color: #F8FAFC;
            display: flex;
            align-items: center;
            justify-content: center;
            height: 100vh;
            margin: 0;
            padding: 20px;
            box-sizing: border-box;
        }
        .card {
            background: #ffffff;
            border-radius: 20px;
            box-shadow: 0 10px 25px rgba(0,0,0,0.06);
            padding: 32px 24px;
            text-align: center;
            max-width: 400px;
            width: 100%;
        }
        .icon {
            width: 64px;
            height: 64px;
            border-radius: 50%;
            display: inline-flex;
            align-items: center;
            justify-content: center;
            font-size: 32px;
            margin-bottom: 20px;
        }
        .icon-success {
            background-color: #ECFDF5;
            color: #059669;
        }
        .icon-fail {
            background-color: #FEF2F2;
            color: #DC2626;
        }
        h2 {
            margin: 0 0 8px;
            color: #0F172A;
            font-size: 20px;
        }
        p {
            color: #64748B;
            font-size: 14px;
            line-height: 1.5;
            margin: 0 0 20px;
        }
        .btn {
            display: inline-block;
            background-color: #2563EB;
            color: #ffffff;
            text-decoration: none;
            padding: 12px 24px;
            border-radius: 12px;
            font-weight: 600;
            font-size: 15px;
            border: none;
            cursor: pointer;
            width: 100%;
            box-sizing: border-box;
        }
    </style>
</head>
<body>
    <div class="card">
        <div class="icon <?php echo $isSuccess ? 'icon-success' : 'icon-fail'; ?>">
            <?php echo $isSuccess ? '✓' : '✕'; ?>
        </div>
        <h2><?php echo $isSuccess ? 'Payment Successful' : 'Payment Status'; ?></h2>
        <p><?php echo $isSuccess ? 'Your Training & Registration Kit payment has been received. Please return to the Jiifit app to continue.' : 'Your payment transaction was completed. Return to the Jiifit app.'; ?></p>
        <button class="btn" onclick="window.close();">Return to App</button>
    </div>
</body>
</html>

