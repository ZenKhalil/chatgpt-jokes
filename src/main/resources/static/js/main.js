const SERVER_URL = 'http://localhost:8080/api/v1/';

document.addEventListener('DOMContentLoaded', function() {
  const formMealPlan = document.getElementById('form-mealplan');
  const formMealPlanLimited = document.getElementById('form-mealplan-limited');
  const formAnswer = document.getElementById('form-answer');

  if (formMealPlan) {
    formMealPlan.addEventListener('submit', getMealPlan);
  }
  if (formMealPlanLimited) {
    formMealPlanLimited.addEventListener('submit', getMealPlanWithRateLimit);
  }
  if (formAnswer) {
    formAnswer.addEventListener('submit', getInfo);
  } 
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
  const spinner = document.getElementById('spinner1');
  const result = document.getElementById('result');
  result.style.color = "black";

  try {
    spinner.style.display = "block";
    const response = await fetch(URL);
    const data = await handleHttpErrors(response);

    // Ensure we have mealPlans and caloricNeeds data in the response
    if (data && data.mealPlans && data.caloricNeeds) {
      const daysOfWeek = ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"];
      let mealPlanText = `Based on your information, you need around ${data.caloricNeeds} calories per day. Here is your weekly meal plan:\n\n`;

      // Loop through each day and append the meal plan for that day
      data.mealPlans.forEach((plan, index) => {
        mealPlanText += `**${daysOfWeek[index]} Meal Plan:**\n\n${plan}\n\n`;
      });

      result.innerText = mealPlanText;
    } else {
      throw new Error('Invalid data format received from the server.');
    }
  } catch (e) {
    console.error(e);
    result.style.color = "red";
    result.innerText = e.message;
  } finally {
    spinner.style.display = "none";
  }
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

async function handleHttpErrors(res) {
  if (!res.ok) {
    const errorResponse = await res.json();
    const msg = errorResponse.message ? errorResponse.message : "No error details provided";
    throw new Error(msg);
  }
  return res.json();
}
