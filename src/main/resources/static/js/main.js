const SERVER_URL = 'http://localhost:8080/api/v1/';

document.addEventListener('DOMContentLoaded', function() {
  const formMealPlan = document.getElementById('form-mealplan');
  const themeToggle = document.getElementById('theme-toggle');
  const themeToggleIcon = document.getElementById('theme-toggle-icon');

  if (formMealPlan) {
    formMealPlan.addEventListener('submit', getMealPlan);
  }

  // Setup theme toggle button
  themeToggle.addEventListener('click', () => {
    document.body.classList.toggle('dark-mode');
    if (document.body.classList.contains('dark-mode')) {
      themeToggleIcon.classList.replace('bi-moon-stars-fill', 'bi-sun-fill');
    } else {
      themeToggleIcon.classList.replace('bi-sun-fill', 'bi-moon-stars-fill');
    }
  });

  // Initialize status icons for all days
  const daysOfWeek = ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"];
  daysOfWeek.forEach(day => {
    const statusElement = document.getElementById(`status${day}`);
    if (statusElement) {
      statusElement.innerHTML = `${day} <span class="status-icon"></span>`;
    }
  });
});

async function getMealPlan(event) {
  event.preventDefault();

  const age = document.getElementById('age').value;
  const weight = document.getElementById('weight').value;
  const height = document.getElementById('height').value;
  const gender = document.getElementById('gender').value;
  const activityLevel = document.getElementById('activityLevel').value;

  const userDetails = `age=${age},weight=${weight},height=${height},gender=${gender},activityLevel=${activityLevel}`;
  const URL = `${SERVER_URL}mealplan?details=${encodeURIComponent(userDetails)}`;

  try {
    const daysOfWeek = ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"];
    daysOfWeek.forEach(day => setSpinnerForDay(day));

    // Initialize check mark timers for each day (including Monday)
    for (let i = 0; i < daysOfWeek.length - 1; i++) {
      setTimeout(() => {
        setCheckMarkForDay(daysOfWeek[i]);
      }, (i + 1) * 20000); // Starts from 20 seconds for Monday and increments by 20 seconds each day
    }

    const response = await fetch(URL);
    const data = await handleHttpErrors(response);

    if (data && data.mealPlans && data.caloricNeeds) {
      // Set check mark for Sunday after the data is fetched
      setCheckMarkForDay('Sunday');
      displayMealPlans(data, daysOfWeek);
    } else {
      throw new Error('Invalid data format received from the server.');
    }
  } catch (e) {
    console.error(e);
    daysOfWeek.forEach(day => setErrorForDay(day, e.message));
  }
}

function displayMealPlans(data, daysOfWeek) {
  // Display meal plans in their respective elements
  data.mealPlans.forEach((plan, index) => {
    const day = daysOfWeek[index];
    const dayElement = document.getElementById(`mealPlan${day}`);
    if (dayElement) {
      dayElement.innerHTML = plan.replace(/\n/g, '<br>');
    }
  });
}

function setSpinnerForDay(day) {
const statusElement = document.getElementById(`status${day}`);
if (statusElement) {
statusElement.innerHTML = `<span class="status-icon spinner-border spinner-border-sm text-primary" role="status"></span> ${day}`;
}
}

function setCheckMarkForDay(day) {
const statusElement = document.getElementById(`status${day}`);
if (statusElement) {
  statusElement.innerHTML = `<span class="status-icon"><img src="https://upload.wikimedia.org/wikipedia/commons/3/3b/Eo_circle_green_checkmark.svg" alt="Check" style="height: 20px; width: 20px;"></span> ${day}`;}
}

function setErrorForDay(day, errorMessage) {
const statusElement = document.getElementById(`status${day}`);
if (statusElement) {
statusElement.innerHTML = `<span class="status-icon text-danger">❌</span> ${day}`;
const dayElement = document.getElementById(`mealPlan${day}`);
if (dayElement) {
dayElement.innerHTML = `<strong>Error:</strong> ${errorMessage}`;
}
}
}

function handleHttpErrors(response) {
if (!response.ok) {
throw new Error(`Network response was not ok. ${response.statusText}`);
}
return response.json();
}



async function getMealPlanWithRateLimit(event) {
  event.preventDefault(); // Prevent the form from reloading the page.

  const URL = `${SERVER_URL}mealplanlimited?details=${document.getElementById('details2').value}`;
  const result2 = document.getElementById('result2');
  const spinner2 = document.getElementById('spinner2');
  result2.style.color = "black";
  result2.innerText = "";
  try {
    spinner2.style.display = "block";
    const response = await fetch(URL).then(handleHttpErrors);
    document.getElementById('result2').innerText = response.answer;
  } catch (e) {
    result2.style.color = "red";
    result2.innerText = e.message;
  } finally {
    spinner2.style.display = "none";
  }
}

async function getInfo(event) {
  event.preventDefault(); // Prevent the form from reloading the page.

  const URL = `${SERVER_URL}info?question=${document.getElementById('the-question').value}`;
  const spinner = document.getElementById('spinner3');
  const result3 = document.getElementById('result3');
  result3.innerText = "";
  result3.style.color = "black";
  try {
    spinner.style.display = "block";
    const reply = await fetch(URL).then(handleHttpErrors);
    document.getElementById('result3').innerHTML = convertToLink(reply.answer);
  } catch (e) {
    result3.style.color = "red";
    result3.innerText = e.message;
  } finally {
    spinner.style.display = "none";
  }

  function convertToLink(str) {
    const urlRegex = /(https?:\/\/[^\s]+)/g;
    return str.replace(urlRegex, function(url) {
      return url.endsWith('.') ? `<a href="${url.slice(0, -1)}" target="_blank">${url.slice(0, -1)}</a>` : `<a href="${url}" target="_blank">${url}</a>`;
    });
  }
}

