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

/**
 * Creates animated particle effects for celebrations
 * Used when user answers questions correctly
 */
function createParticles() {
    for (let i = 0; i < 10; i++) {
        const particle = document.createElement('div');
        particle.classList.add('particle');
        particle.style.width = `${Math.random() * 10 + 5}px`;
        particle.style.height = particle.style.width;
        particle.style.left = `${Math.random() * 100}%`;
        particle.style.bottom = '0';
        particle.style.animationDuration = `${Math.random() * 3 + 2}s`;
        document.body.appendChild(particle);

        // Remove particle after animation completes
        setTimeout(() => {
            particle.remove();
        }, 5000);
    }
}

// Export the functions for testing environments while keeping browser compatibility
if (typeof module !== 'undefined' && typeof module.exports !== 'undefined') {
    module.exports = {
        getRandomQuestions,
        createParticles
    };
}