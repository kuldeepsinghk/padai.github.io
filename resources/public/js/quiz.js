/**
 * Math Quiz Utility Functions
 */

// Function to randomly select n questions from an array
function getRandomQuestions(questions, n) {
    // Make a copy of the array to avoid modifying the original
    const shuffled = [...questions];
    
    // Fisher-Yates (Knuth) shuffle algorithm
    for (let i = shuffled.length - 1; i > 0; i--) {
        const j = Math.floor(Math.random() * (i + 1));
        [shuffled[i], shuffled[j]] = [shuffled[j], shuffled[i]];
    }
    
    // Return the first n elements
    return shuffled.slice(0, n);
}

// Export the function for testing environments while keeping browser compatibility
if (typeof module !== 'undefined' && typeof module.exports !== 'undefined') {
    module.exports = {
        getRandomQuestions
    };
}