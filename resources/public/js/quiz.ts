/**
 * Math Quiz Utility Functions
 */

// Define enum for grades for better type safety
enum Grade {
  SixthGrade = "6th",
  SeventhGrade = "7th",
  TenthGrade = "10th"
}

// Define types for curriculum data
interface Chapter {
  key_topics: string[];
}

interface CurriculumData {
  [grade: string]: {
    [subject: string]: {
      [strand: string]: {
        [chapterKey: string]: Chapter;
      };
    };
  }
}

/**
 * Gets available subjects for a specific grade
 * @param grade The school grade
 * @param curriculumData The loaded curriculum data
 * @returns Array of available subjects for the grade
 */
function getSubjectsForGrade(grade: string, curriculumData: CurriculumData): string[] {
  if (!curriculumData[grade]) {
    console.error(`No data found for grade: ${grade}`);
    return [];
  }
  
  return Object.keys(curriculumData[grade]);
}

// Function to randomly select n questions from an array
function getRandomQuestions<T>(questions: T[], n: number): T[] {
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
function createParticles(): void {
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

/**
 * Loads curriculum data from grade-subjects.json file
 * @returns {Promise<CurriculumData>} The curriculum data object
 * @throws {Error} If data loading fails
 */
async function loadCurriculumDataFromQuizJS(): Promise<CurriculumData> {
    try {
        const response = await fetch('grade-subjects.json');
        if (!response.ok) {
            throw new Error('Failed to load curriculum data');
        }
        const data: CurriculumData = await response.json();
        console.log('Curriculum data loaded:', data);
        return data;
    } catch (error) {
        console.error('Error loading curriculum data:', error);
        throw error;
    }
}

// Export the functions for testing environments while keeping browser compatibility
if (typeof module !== 'undefined' && typeof module.exports !== 'undefined') {
    module.exports = {
        getRandomQuestions,
        createParticles,
        loadCurriculumDataFromQuizJS,
        getSubjectsForGrade
    };
}

// Make functions available globally for browser access
if (typeof window !== 'undefined') {
    (window as any).getRandomQuestions = getRandomQuestions;
    (window as any).createParticles = createParticles;
    (window as any).loadCurriculumDataFromQuizJS = loadCurriculumDataFromQuizJS;
    (window as any).getSubjectsForGrade = getSubjectsForGrade;
}
