const output = document.getElementById("output");
const generateBtn = document.getElementById("generateBtn");
const difficulty = document.getElementById("difficulty");

generateBtn?.addEventListener("click", async () => {
  output.textContent = "Generating quest...";
  try {
    const res = await fetch("/api/challenge/generate", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ difficulty: difficulty.value })
    });

    if (!res.ok) {
      throw new Error(`Request failed with ${res.status}`);
    }

    const data = await res.json();
    output.textContent = JSON.stringify(data, null, 2);
  } catch (error) {
    output.textContent = `Failed: ${error.message}`;
  }
});
