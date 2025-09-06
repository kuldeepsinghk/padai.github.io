/**
 * Unit tests for quiz.js functions
 */

// Import the functions to test
const { getRandomQuestions, loadCurriculumDataFromQuizJS } = require('../../resources/public/js/quiz');

// Mock the fetch API for testing
global.fetch = jest.fn();

// Setup console mocks to avoid cluttering test output
global.console.log = jest.fn();
global.console.error = jest.fn();

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

describe('loadCurriculumDataFromQuizJS', () => {
  // Reset mocks before each test
  beforeEach(() => {
    fetch.mockClear();
    console.log.mockClear();
    console.error.mockClear();
  });

  // Test successful data loading
  test('should load curriculum data successfully', async () => {
    // Mock data for the test
    const mockData = {
      "6th": {
        "Mathematics": ["algebra", "geometry"],
        "Science": ["physics", "biology"]
      },
      "10th": {
        "Mathematics": ["calculus", "statistics"],
        "Science": ["chemistry", "physics"]
      }
    };

    // Mock the fetch response
    fetch.mockResolvedValueOnce({
      ok: true,
      json: async () => mockData
    });

    // Call the function
    const result = await loadCurriculumDataFromQuizJS();

    // Verify fetch was called with correct URL
    expect(fetch).toHaveBeenCalledWith('grade-subjects.json');
    
    // Verify the correct data was returned
    expect(result).toEqual(mockData);
    
    // Verify log was called
    expect(console.log).toHaveBeenCalledWith('Curriculum data loaded:', mockData);
  });

  // Test failed fetch due to network error
  test('should handle network errors', async () => {
    // Mock a network error
    const networkError = new Error('Network error');
    fetch.mockRejectedValueOnce(networkError);

    // Call the function and expect it to throw
    await expect(loadCurriculumDataFromQuizJS()).rejects.toThrow('Network error');
    
    // Verify error was logged
    expect(console.error).toHaveBeenCalledWith('Error loading curriculum data:', networkError);
  });

  // Test failed fetch due to bad response
  test('should handle non-ok response', async () => {
    // Mock a non-ok response
    fetch.mockResolvedValueOnce({
      ok: false,
      status: 404,
      statusText: 'Not Found'
    });

    // Call the function and expect it to throw
    await expect(loadCurriculumDataFromQuizJS()).rejects.toThrow('Failed to load curriculum data');
  });
  
  // Test failed JSON parsing
  test('should handle invalid JSON response', async () => {
    // Mock a response with invalid JSON
    const jsonError = new Error('Invalid JSON');
    fetch.mockResolvedValueOnce({
      ok: true,
      json: async () => { throw jsonError; }
    });

    // Call the function and expect it to throw
    await expect(loadCurriculumDataFromQuizJS()).rejects.toThrow('Invalid JSON');
    
    // Verify error was logged
    expect(console.error).toHaveBeenCalledWith('Error loading curriculum data:', jsonError);
  });
});
