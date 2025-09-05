/**
 * Unit tests for quiz.js functions
 */

// Import the functions to test
const { getRandomQuestions } = require('../../resources/public/js/quiz');

describe('getRandomQuestions', () => {
  // Test that the function returns the correct number of questions
  test('should return the requested number of questions', () => {
    const questions = [
      { id: 1, question: 'Q1' },
      { id: 2, question: 'Q2' },
      { id: 3, question: 'Q3' },
      { id: 4, question: 'Q4' },
      { id: 5, question: 'Q5' }
    ];
    
    const result = getRandomQuestions(questions, 3);
    expect(result.length).toBe(3);
  });

  // Test that the function doesn't modify the original array
  test('should not modify the original array', () => {
    const questions = [
      { id: 1, question: 'Q1' },
      { id: 2, question: 'Q2' },
      { id: 3, question: 'Q3' }
    ];
    
    const originalQuestions = [...questions];
    getRandomQuestions(questions, 2);
    
    expect(questions).toEqual(originalQuestions);
  });

  // Test that the function returns all questions when n equals the array length
  test('should return all questions when n equals array length', () => {
    const questions = [
      { id: 1, question: 'Q1' },
      { id: 2, question: 'Q2' },
      { id: 3, question: 'Q3' }
    ];
    
    const result = getRandomQuestions(questions, 3);
    expect(result.length).toBe(3);
    
    // Check that all original questions are included (though possibly in different order)
    questions.forEach(q => {
      expect(result.some(r => r.id === q.id)).toBeTruthy();
    });
  });

  // Test that the function returns a subset of the original questions
  test('should return a subset of the original questions', () => {
    const questions = [
      { id: 1, question: 'Q1' },
      { id: 2, question: 'Q2' },
      { id: 3, question: 'Q3' },
      { id: 4, question: 'Q4' },
      { id: 5, question: 'Q5' }
    ];
    
    const result = getRandomQuestions(questions, 3);
    
    // Each returned question should be from the original array
    result.forEach(q => {
      expect(questions.some(original => original.id === q.id)).toBeTruthy();
    });
  });

  // Test edge cases
  test('should handle edge cases', () => {
    // Empty array
    expect(getRandomQuestions([], 5)).toEqual([]);
    
    // n = 0
    const questions = [{ id: 1 }, { id: 2 }];
    expect(getRandomQuestions(questions, 0)).toEqual([]);
    
    // n > array length
    const result = getRandomQuestions(questions, 5);
    expect(result.length).toBe(2); // Should return only what's available
  });
});
