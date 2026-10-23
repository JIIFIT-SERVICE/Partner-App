<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0, viewport-fit=cover">
    <title>JIIFIT | Massage Services</title>
    <!-- Font Awesome 6 -->
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0-beta3/css/all.min.css">
<style>
/* ==========================================
   CONFIRMATION OVERLAY & POPUP STYLES
   Separated for easier management
   ========================================== */

/* ----- Overlay Background ----- */
.confirmation-overlay {
    display: none;
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background: rgba(0, 0, 0, 0.7);
    z-index: 2000;
    justify-content: center;
    align-items: center;
}

/* ----- Popup Container ----- */
.confirmation-popup {
    background: #fff;
    padding: 30px 25px;
    border-radius: 15px;
    max-width: 380px;
    width: 90%;
    text-align: center;
    box-shadow: 0 15px 50px rgba(0, 0, 0, 0.3);
    animation: popIn 0.5s ease-out;
}

/* ----- Popup Animation ----- */
@keyframes popIn {
    0% {
        transform: scale(0.5);
        opacity: 0;
    }
    70% {
        transform: scale(1.05);
    }
    100% {
        transform: scale(1);
        opacity: 1;
    }
}

/* ----- Icon Container ----- */
.confirmation-popup .icon-container {
    font-size: 70px;
    color: #4CAF50;
    margin: 0 0 15px;
}

/* ----- Main Heading Text ----- */
.confirmation-popup .message-text {
    font-size: 22px;
    color: #2c3e50;
    margin: 10px 0;
    font-weight: 600;
}

/* ----- Sub Message (Dynamic) ----- */
.confirmation-popup .sub-message {
    color: #7f8c8d;
    font-size: 14px;
    margin: 10px 0 20px;
    line-height: 1.6;
    min-height: 40px;
    padding: 8px 12px;
    background: #f8f9fa;
    border-radius: 6px;
    border-left: 3px solid #4CAF50;
}

/* ----- Booking Details Box ----- */
.confirmation-popup .booking-details {
    background: #f8f9fa;
    padding: 12px;
    border-radius: 8px;
    margin: 15px 0;
    text-align: left;
}

.confirmation-popup .booking-details p {
    margin: 5px 0;
    font-size: 14px;
    color: #333;
}

.confirmation-popup .booking-details strong {
    color: #2c3e50;
}

/* ----- OK / Share Button ----- */
.btn-ok {
    background: #4CAF50;
    color: white;
    border: none;
    padding: 12px 50px;
    font-size: 18px;
    border-radius: 25px;
    cursor: pointer;
    transition: all 0.3s ease;
    font-weight: 600;
    margin-top: 10px;
}

.btn-ok:hover {
    background: #45a049;
    transform: scale(1.05);
}

.btn-ok:active {
    transform: scale(0.95);
}

.btn-ok:disabled {
    opacity: 0.6;
    cursor: not-allowed;
}


/* ==========================================
   RESPONSIVE - CONFIRMATION OVERLAY
   ========================================== */

/* ----- Mobile L (≤ 480px) ----- */
@media (max-width: 480px) {
    .confirmation-popup {
        padding: 20px 15px;
        max-width: 320px;
    }
    
    .confirmation-popup .message-text {
        font-size: 18px;
    }
    
    .confirmation-popup .icon-container {
        font-size: 50px;
    }
    
    .btn-ok {
        padding: 10px 40px;
        font-size: 16px;
    }
}

/* ----- Mobile M (≤ 375px) ----- */
@media (max-width: 375px) {
    .confirmation-popup {
        padding: 15px 12px;
        max-width: 280px;
    }
    
    .confirmation-popup .message-text {
        font-size: 16px;
    }
    
    .btn-ok {
        padding: 8px 30px;
        font-size: 14px;
    }
}

/* ----- Mobile S (≤ 320px) ----- */
@media (max-width: 320px) {
    .confirmation-popup {
        padding: 15px 10px;
        max-width: 250px;
    }
    
    .confirmation-popup .message-text {
        font-size: 15px;
    }
    
    .btn-ok {
        padding: 8px 25px;
        font-size: 13px;
    }
}
</style>
</head>
<body>
        <div id="confirmationOverlay" class="confirmation-overlay" style="display:none;">
            <div class="confirmation-popup">
                <div class="icon-container">
                    <i class="fa-solid fa-circle-check"></i> 
                </div>
                <p class="message-text">Booking Confirmed!</p>
                
                <!-- Dynamic sub-message with receiver name and relationship -->
                <p class="sub-message" id="subMessage">Loading booking details...</p>
                
                <button class="btn-ok" id="btnConfirmOk"><i class="fab fa-whatsapp"></i>Share Booking Details</button>
            </div>
        </div>
        
<script>
    let bookingId = '';
    let bookingSlot = '';
    let receiverName = '';
    let relationship = '';
    let therapistName = '';
    let orderItems = '';
    let totalPrice = '';
    let discountPrice = '';
    let gst = '';
    let paymentMode = '';
    let bookingOTP = '';
    let receiverWhatsApp = '';
    var decodedUserId = 11;
    
// Check if isLoggedIn is defined
        if (typeof isLoggedIn !== 'undefined' && isLoggedIn) {
            // console.log("Login 1: ", isLoggedIn);
            fetchBookingData(decodedUserId);
        } else {
            // console.log('User not logged in');
        }
    function fetchBookingData(userId) {
    // console.log("Fetching booking data for user ID:", userId);
    
     const basePath = getBasePath();
    let url = basePath + 'checkBookingStatus.php?decoded_id=' + userId;
    // console.log("Request URL:", url);
    
    fetch(url, {
        method: 'GET',
        headers: {
            'Content-Type': 'application/json',
        }
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('Network response was not ok');
        }
        return response.json();
    })
    .then(data => {
        // console.log("Response data:", data);
        
        if (data.success) {
            const bookingData = data.data;
            const who_take_status = bookingData.who_take_status;
            const reciver_name = bookingData.reciver_name || 'Guest';
            const relationship_val = bookingData.relationship || 'Client';
            
            // console.log("Receiver name:", reciver_name);
            // console.log("Relationship:", relationship_val);
            // console.log("Who take status:", who_take_status);
            
            // ডেটা sessionStorage এ সংরক্ষণ করুন
            sessionStorage.setItem('bookingId', bookingData.bookingId || 'N/A');
            sessionStorage.setItem('bookingSlot', bookingData.order_date + ' at ' + bookingData.start_time);
            sessionStorage.setItem('receiverName', reciver_name);
            sessionStorage.setItem('relationship', relationship_val);
            sessionStorage.setItem('therapistName', bookingData.therapist_name || 'Not Assigned');
            sessionStorage.setItem('orderItems', bookingData.order_items || 'Standard Massage Service');
            sessionStorage.setItem('totalPrice', bookingData.total_price || '0');
            sessionStorage.setItem('discountPrice', bookingData.discunt_price || '0');
            sessionStorage.setItem('gst', bookingData.GST || '0');
            sessionStorage.setItem('paymentMode', bookingData.payment_mode || 'UPI / Cash after massage');
            sessionStorage.setItem('bookingOTP', bookingData.booking_otp || 'Will be shared shortly');
            sessionStorage.setItem('receiverWhatsApp', bookingData.r_wp_num || '');
            
            // গ্লোবাল ভেরিয়েবলেও সেট করুন
            bookingId = bookingData.bookingId;
            bookingSlot = bookingData.order_date + ' at ' + bookingData.start_time;
            receiverName = reciver_name;
            relationship = relationship_val;
            therapistName = bookingData.therapist_name || 'Not Assigned';
            orderItems = bookingData.order_items || 'Standard Massage Service';
            totalPrice = bookingData.total_price || '0';
            discountPrice = bookingData.discunt_price || '0';
            gst = bookingData.GST || '0';
            paymentMode = bookingData.payment_mode || 'UPI / Cash after massage';
            bookingOTP = bookingData.booking_otp || 'Will be shared shortly';
            receiverWhatsApp = bookingData.r_wp_num || '';
            
            // who_take_status এর উপর ভিত্তি করে overlay দেখান
            if (who_take_status == 1) {
                // Confirmation overlay দেখান
                document.getElementById("confirmationOverlay").style.display = "flex";
                
                // subMessage আপডেট করুন
                const subMessage = document.getElementById('subMessage');
                if (subMessage) {
                    subMessage.textContent = `Share the booking confirmation, therapist details, and OTP with ${reciver_name}}`;
                    // console.log("SubMessage updated:", subMessage.textContent);/**/
                }
                // Booking details section দেখান
                const detailsSection = document.querySelector('.booking-details');
                if (detailsSection) {
                    detailsSection.style.display = 'block';
                }
                
            } else if (who_take_status == 0) {
                // Massage type overlay দেখান
                document.getElementById("confirmationOverlay").style.display = "none";
            } else {
                // console.log('No booking data found with valid status');
            }
        } else {
            // console.log(data.message || 'No booking found');
        }
    })
    .catch(error => {
        console.error('Error fetching booking data:', error);
    });
}

// ============= ডম রেডি =============
document.addEventListener("DOMContentLoaded", function() {
    const btnOk = document.getElementById("btnConfirmOk");
    if (btnOk) {
        btnOk.addEventListener("click", handleButtonClick);
    }
});

// ============= হ্যান্ডেল বাটন ক্লিক =============
async function handleButtonClick() {
    const btnOk = document.getElementById('btnConfirmOk');
    
    if (!btnOk) {
        console.error("Button not found");
        return;
    }
    
    // Disable button to prevent multiple clicks
    btnOk.disabled = true;
    btnOk.textContent = 'Processing...';
    
    try {
        // 1. Call the PHP file first
        // console.log("Calling PHP file...");
        const phpResponse = await callPhpFile();
        // console.log("PHP Response:", phpResponse);
        
        // 2. Then share on WhatsApp
        btnOk.textContent = 'Opening WhatsApp...';
        shareOnWhatsApp();
        
        // 3. Redirect after a short delay
           setTimeout(() => {
            window.location.reload(); // This will refresh the current page
        }, 3000);
        
    } catch (error) {
        console.error('Error in button click handler:', error);
        // Even if PHP fails, try to open WhatsApp
        btnOk.textContent = 'Opening WhatsApp...';
        shareOnWhatsApp();
        
        setTimeout(() => {
             window.location.reload(); // This will refresh the current page
        }, 3000);
    }
}

// ============= শেয়ার অন হোয়াটসঅ্যাপ =============
function shareOnWhatsApp() {
    const message = generateWhatsAppMessage();
    // console.log("WhatsApp Message generated");
    // 
    // Get receiver's WhatsApp number from sessionStorage
    const storedReceiverWhatsApp = sessionStorage.getItem('receiverWhatsApp') || receiverWhatsApp || '';
    
    let whatsappUrl;
    if (storedReceiverWhatsApp && storedReceiverWhatsApp !== '') {
        // Remove any non-digit characters from phone number
        const cleanPhone = storedReceiverWhatsApp.replace(/\D/g, '');
        // Add country code if not present (assuming India +91)
        const phoneWithCode = cleanPhone.startsWith('91') ? cleanPhone : `91${cleanPhone}`;
        whatsappUrl = `https://wa.me/${phoneWithCode}?text=${message}`;
        // console.log("WhatsApp URL with phone:", whatsappUrl);
    } else {
        // If no receiver number, open WhatsApp with message only - user selects contact
        whatsappUrl = `https://wa.me/?text=${message}`;
        // console.log("WhatsApp URL without phone:", whatsappUrl);
    }
    
    // Open WhatsApp
    window.open(whatsappUrl, '_blank');
}

// ============= জেনারেট হোয়াটসঅ্যাপ মেসেজ =============
function generateWhatsAppMessage() {
    // Get values from sessionStorage
    const storedBookingId = sessionStorage.getItem('bookingId') || bookingId || 'N/A';
    const storedSlot = sessionStorage.getItem('bookingSlot') || bookingSlot || 'N/A';
    const storedReceiverName = sessionStorage.getItem('receiverName') || receiverName || 'Valued Customer';
    const storedRelationship = sessionStorage.getItem('relationship') || relationship || 'Your Family Member';
    const storedTherapistName = sessionStorage.getItem('therapistName') || therapistName || 'To be assigned';
    const storedOrderItems = sessionStorage.getItem('orderItems') || orderItems || 'Standard Massage Service';
    const storedTotalPrice = sessionStorage.getItem('totalPrice') || totalPrice || '0';
    const storedDiscountPrice = sessionStorage.getItem('discountPrice') || discountPrice || '0';
    const storedGst = sessionStorage.getItem('gst') || gst || '0';
    const storedPaymentMode = sessionStorage.getItem('paymentMode') || paymentMode || 'UPI / Cash after massage';
    const storedOTP = sessionStorage.getItem('bookingOTP') || bookingOTP || 'Will be shared shortly';
    
    // console.log("Generating message with data:", {
    //     bookingId: storedBookingId,
    //     receiverName: storedReceiverName,
    //     relationship: storedRelationship,
    //     therapistName: storedTherapistName,
    //     totalPrice: storedTotalPrice
    // });
    
    // Determine time of day greeting
    const currentHour = new Date().getHours();
    let greeting = '';
    if (currentHour < 12) greeting = 'Morning';
    else if (currentHour < 17) greeting = 'Afternoon';
    else greeting = 'Evening';
    
    // Parse order items
    let massageDetails = '';
    let serviceCharges = 0;
    let gstAmount = parseFloat(storedGst) || 0;
    let totalAmount = parseFloat(storedTotalPrice) || 0;
    
    if (storedOrderItems && storedOrderItems !== 'N/A') {
        const items = storedOrderItems.split(', ');
        items.forEach((item) => {
            const match = item.match(/^(.+?)\s*\((\d+)\s*x\s*([\d.]+)\)$/);
            if (match) {
                const itemName = match[1];
                const quantity = match[2];
                const price = parseFloat(match[3]);
                serviceCharges += price * parseInt(quantity);
                massageDetails += `• ${itemName} (${quantity} Session) x ${quantity} = ₹${(price * parseInt(quantity)).toFixed(2)}\n`;
            } else {
                massageDetails += `• ${item}\n`;
            }
        });
    }
    
    // Calculate GST if not provided
    if (gstAmount === 0 && serviceCharges > 0) {
        gstAmount = serviceCharges * 0.05;
    }
    
    // Calculate total if not set
    if (totalAmount === 0 && serviceCharges > 0) {
        totalAmount = serviceCharges + gstAmount;
        if (storedDiscountPrice && parseFloat(storedDiscountPrice) > 0) {
            totalAmount = totalAmount - parseFloat(storedDiscountPrice);
        }
    }
    
    const paymentModeDisplay = storedPaymentMode || 'UPI / Cash after massage';
    
    // Build the message - TEXT ONLY, NO IMAGES
    let message = `Hello ${storedReceiverName}, Good ${greeting}! \n\n`;
    message += `A wellness massage session has been arranged for you by ${storedRelationship} with JIIFIT.\n\n`;
    message += `Jiifit – India's 1st specialised massage company for client's place \n\n`;
    message += `Booking Summary:\n`;
    message += `• Booking ID: #${storedBookingId}\n`;
    
    if (storedSlot && storedSlot !== 'N/A') {
        message += `• Booking Date & Time: ${storedSlot}\n`;
    }
    
    message += `\nPlease be available at the scheduled booking time for a smooth and relaxing experience.\n\n`;
    message += `Selected Massage:\n`;
    
    if (massageDetails) {
        message += massageDetails;
    } else {
        message += `• ${storedOrderItems}\n`;
    }
    
    message += `\n• Service Charges: ₹${serviceCharges.toFixed(2)}\n`;
    message += `• GST (5%): ₹${gstAmount.toFixed(2)}\n`;
    
    if (storedDiscountPrice && parseFloat(storedDiscountPrice) > 0) {
        message += `• Discount: -₹${parseFloat(storedDiscountPrice).toFixed(2)}\n`;
    }
    
    message += `• Total Amount Payable: ₹${totalAmount.toFixed(2)}\n\n`;
    message += `Payment Mode:\n${paymentModeDisplay}\n\n`;
    
    message += `Therapist Details:\n`;
    message += `• Name: ${storedTherapistName}\n`;
    
    if (storedOTP && storedOTP !== 'Will be shared shortly') {
        message += `\nOTP: ${storedOTP}\n`;
    } else {
        message += `\nOTP: ${storedOTP}\n`;
    }
    message += `Please share this OTP with the therapist before starting your session.\n\n`;
    
    message += `Your therapist may contact you before arrival if required.\n\n`;
    message += `Want to know more?\n`;
    message += `• View therapist profile\n`;
    message += `• Cancel or reschedule booking\n`;
    message += `• Booking history\n`;
    message += `• Therapist contact details\n\n`;
    message += `Visit jiifit.com\n\n`;
    message += `Thank you for choosing Jiifit!\n`;
    message += `We look forward to providing you with a relaxing and rejuvenating experience in the comfort of your place.\n\n`;
    
    // Personalized sign-off
    let signOff = `Warm regards,\nJiifit team`;
    if (storedRelationship && storedRelationship !== 'Your Family Member') {
        const relationshipLower = storedRelationship.toLowerCase();
        if (relationshipLower.includes('husband') || relationshipLower.includes('wife') || 
            relationshipLower.includes('father') || relationshipLower.includes('mother')) {
            signOff = `Warm regards,\nYour ${storedRelationship}`;
        } else if (relationshipLower.includes('friend')) {
            signOff = `Warm regards,\nYour friend`;
        }
    }
    message += signOff;
    
    return encodeURIComponent(message);
}

// ============= কল পিএইচপি ফাইল =============
async function callPhpFile() {
    try {
        // Get booking ID from sessionStorage
        const storedBookingId = sessionStorage.getItem('bookingId') || bookingId || '';
        
        if (!storedBookingId || storedBookingId === 'N/A') {
            console.warn('No valid booking ID found');
            return null;
        }
        
        // console.log("Calling PHP with booking ID:", storedBookingId);
        
        // Call the PHP file with the booking ID
        const response = await fetch("./php/payment_gateway/updateBookingStatus.php?booking_id=" + storedBookingId, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json'
            }
        });
        
        if (response.ok) {
            const data = await response.json();
            // console.log('PHP file response:', data);
            return data;
        } else {
            console.error('Failed to call PHP file:', response.status);
            return null;
        }
    } catch (error) {
        console.error('Error calling PHP file:', error);
        return null;
    }
}

function getBasePath() {
    // বর্তমান URL থেকে পাথ বের করা
    const currentPath = window.location.pathname;
    const pathParts = currentPath.split('/');
    
    // যদি current file root directory তে থাকে
    if (pathParts.length <= 2) {
        return './';
    }
    
    // ফাইলটি কোথায় আছে তার উপর ভিত্তি করে পাথ নির্ধারণ
    const currentFile = pathParts[pathParts.length - 1];
    
    // যদি current file "admin" বা "dashboard" ফোল্ডারে থাকে
    if (currentPath.includes('/admin/') || currentPath.includes('/dashboard/')) {
        return '../';
    }
    
    // যদি current file "user" বা "profile" ফোল্ডারে থাকে
    if (currentPath.includes('/user/') || currentPath.includes('/profile/')) {
        return '../';
    }
    
        if (currentPath.includes('/massage-at-home/')) {
        return '../';
      }
    
    // ডিফল্ট
    return './';
}
</script>

</body>
</html>
