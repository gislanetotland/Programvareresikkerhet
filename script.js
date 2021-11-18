
async function subscribe(id,vers) {
  let response = await fetch("/subscribe/" + id +"?version=" + vers);

  if (response.status == 502) {
    // Status 502 is a connection timeout error,
    // may happen when the connection was pending for too long,
    // and the remote server or a proxy closed it
    // let's reconnect
    await subscribe();
  } else if (response.status != 200) {
    // An error - let's show it
    alert(response.statusText);
    // Reconnect in one second
    await new Promise(resolve => setTimeout(resolve, 1000));
    await subscribe();
  } else {
    // Get and show the message
    let message = await response.text();
    let lineend = message.indexOf("\n")
    let newvers = message.substr(0, lineend);
    let html = message.substr(lineend+1);
    let chan = document.getElementById("channel");
    let chanevents = document.getElementById("chanevents");
    chan.replaceChild(htmlToElem(html),chanevents);
    // Call subscribe() again to get the next message
    await subscribe(id,newvers);
  }
}


function htmlToElem(html) {
  let temp = document.createElement('template');
  html = html.trim(); // Never return a space text node as a result
  temp.innerTextContent = html;
  return temp.content.firstChild;
}

function submitOnEnter(event){
    if(event.which === 13 && !event.shiftKey){
        event.target.form.submit();
        //event.target.form.dispatchEvent(new Event("submit", {cancelable: true}));
        event.preventDefault();
    }
}
    var myInput = document.getElementById("psw");
      var letter = document.getElementById("letter");
      var capital = document.getElementById("capital");
      var number = document.getElementById("number");
      var length = document.getElementById("length");
      var password = document.getElementById("psw");
      var confirm_password = document.getElementById("confirm_password");

      // When the user clicks on the password field, show the message box
      myInput.onfocus = function() {
          document.getElementById("message").style.display = "block";
      }

      // When the user clicks outside of the password field, hide the message box
      myInput.onblur = function() {
          document.getElementById("message").style.display = "none";
      }

      // When the user starts to type something inside the password field
      myInput.onkeyup = function() {
          // Validate lowercase letters
          var lowerCaseLetters = /[a-z]/g;
          if(myInput.value.match(lowerCaseLetters)) {
              letter.classList.remove("invalid");
              letter.classList.add("valid");
          } else {
              letter.classList.remove("valid");
              letter.classList.add("invalid");
          }

          // Validate capital letters
          var upperCaseLetters = /[A-Z]/g;
          if(myInput.value.match(upperCaseLetters)) {
              capital.classList.remove("invalid");
              capital.classList.add("valid");
          } else {
              capital.classList.remove("valid");
              capital.classList.add("invalid");
          }

          // Validate numbers
          var numbers = /[0-9]/g;
          if(myInput.value.match(numbers)) {
              number.classList.remove("invalid");
              number.classList.add("valid");
          } else {
              number.classList.remove("valid");
              number.classList.add("invalid");
          }

          // Validate length
          if(myInput.value.length >= 8) {
              length.classList.remove("invalid");
              length.classList.add("valid");
          } else {
              length.classList.remove("valid");
              length.classList.add("invalid");
          }


          function validatePassword(){
              if(password.value != confirm_password.value) {
                  confirm_password.setCustomValidity("Passwords Don't Match");
              } else {
                  confirm_password.setCustomValidity('');
              }
          }

          password.onchange = validatePassword;
          confirm_password.onkeyup = validatePassword;
      }
