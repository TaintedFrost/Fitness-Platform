const registerForm = document.getElementById("registerForm");

if (registerForm) {

    registerForm.addEventListener("submit", async (e) => {

        e.preventDefault();

        const data = {
            username: document.getElementById("username").value,
            email: document.getElementById("email").value,
            password: document.getElementById("password").value,
            fitnessGoal: document.getElementById("fitnessGoal").value,
            experienceLevel: document.getElementById("experienceLevel").value
        };

        const response = await fetch("http://localhost:8080/auth/register", {

            method: "POST",

            headers: {
                "Content-Type": "application/json"
            },

            body: JSON.stringify(data)
        });

        const message = await response.text();

        document.getElementById("message").innerText = message;
    });
}

const loginForm = document.getElementById("loginForm");

if (loginForm) {

    loginForm.addEventListener("submit", async (e) => {

        e.preventDefault();

        const data = {
            email: document.getElementById("loginEmail").value,
            password: document.getElementById("loginPassword").value
        };

        const response = await fetch("http://localhost:8080/auth/login", {

            method: "POST",

            headers: {
                "Content-Type": "application/json"
            },

            body: JSON.stringify(data)
        });

        const result = await response.json();

        localStorage.setItem("token", result.token);

        document.getElementById("loginMessage").innerText =
            "Login successful!";
    });
}