import { render, screen, waitFor, fireEvent } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { vi } from 'vitest'
import App from './App'

const mockSkillsResponse = {
  count: 5,
  skills: ['CalculatorSkill', 'MockSearchSkill', 'OsqueryMCPSkill', 'SummarizeSkill', 'WeatherSkill']
}

const mockExecuteResponse = {
  goal: 'Calculate 2 + 2',
  skill: 'CalculatorSkill',
  trace: [],
  finalOutput: 'Result: 4'
}

describe('App', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    // Mock fetch globally
    vi.stubGlobal('fetch', vi.fn())
  })

  it('renders the app correctly', () => {
    ;(fetch as any).mockResolvedValueOnce({
      json: () => Promise.resolve(mockSkillsResponse),
    })

    render(<App />)
    expect(screen.getByText('Spring AI Agent Interface')).toBeInTheDocument()
  })

  it('increments count on button click', async () => {
    ;(fetch as any).mockResolvedValueOnce({
      json: () => Promise.resolve(mockSkillsResponse),
    })

    const user = userEvent.setup()
    render(<App />)
    const button = screen.getByRole('button', { name: /count is 0/i })
    await user.click(button)
    expect(screen.getByText('count is 1')).toBeInTheDocument()
  })

  it('fetches message from server', async () => {
    const mockMessageResponse = { message: 'Hello from AI server!' }
    ;(fetch as any)
      .mockResolvedValueOnce({
        json: () => Promise.resolve(mockSkillsResponse),
      })
      .mockResolvedValueOnce({
        json: () => Promise.resolve(mockMessageResponse),
      })

    const user = userEvent.setup()
    render(<App />)
    const button = screen.getByRole('button', { name: /Test Server Connection/i })
    await user.click(button)

    await waitFor(() => {
      expect(screen.getByText('Server says: Hello from AI server!')).toBeInTheDocument()
    })
  })

  it('handles fetch error', async () => {
    ;(fetch as any)
      .mockResolvedValueOnce({
        json: () => Promise.resolve(mockSkillsResponse),
      })
      .mockRejectedValueOnce(new Error('Network error'))

    const user = userEvent.setup()
    render(<App />)
    const button = screen.getByRole('button', { name: /Test Server Connection/i })
    await user.click(button)

    await waitFor(() => {
      expect(screen.getByText('Server says: Error connecting to server')).toBeInTheDocument()
    })
  })

  it('loads skills on mount', async () => {
    ;(fetch as any).mockResolvedValueOnce({
      json: () => Promise.resolve(mockSkillsResponse),
    })

    render(<App />)

    await waitFor(() => {
      expect(screen.getByText('AI Skills (5)')).toBeInTheDocument()
    })
  })

  it('opens and closes hamburger menu', async () => {
    ;(fetch as any).mockResolvedValueOnce({
      json: () => Promise.resolve(mockSkillsResponse),
    })

    const user = userEvent.setup()
    render(<App />)

    // Menu should not be open initially
    const menu = screen.getByRole('complementary', { hidden: true }) // skills-menu
    expect(menu).not.toHaveClass('open')

    // Click hamburger button to open menu
    const hamburgerBtn = screen.getByLabelText('Toggle skills menu')
    await user.click(hamburgerBtn)

    await waitFor(() => {
      expect(menu).toHaveClass('open')
    })

    // Click close button to close menu
    const closeBtn = screen.getByText('×')
    await user.click(closeBtn)

    await waitFor(() => {
      expect(menu).not.toHaveClass('open')
    })
  })

  it('displays skills in menu', async () => {
    ;(fetch as any).mockResolvedValueOnce({
      json: () => Promise.resolve(mockSkillsResponse),
    })

    const user = userEvent.setup()
    render(<App />)

    const hamburgerBtn = screen.getByLabelText('Toggle skills menu')
    await user.click(hamburgerBtn)

    await waitFor(() => {
      expect(screen.getByText('Calculator')).toBeInTheDocument()
      expect(screen.getByText('MockSearch')).toBeInTheDocument()
      expect(screen.getByText('OsqueryMCP')).toBeInTheDocument()
      expect(screen.getByText('Summarize')).toBeInTheDocument()
      expect(screen.getByText('Weather')).toBeInTheDocument()
    })
  })

  it('selects skill and shows executor', async () => {
    ;(fetch as any).mockResolvedValueOnce({
      json: () => Promise.resolve(mockSkillsResponse),
    })

    const user = userEvent.setup()
    render(<App />)

    const hamburgerBtn = screen.getByLabelText('Toggle skills menu')
    await user.click(hamburgerBtn)

    await waitFor(() => {
      const calculatorSkill = screen.getByText('Calculator')
      fireEvent.click(calculatorSkill)
    })

    await waitFor(() => {
      expect(screen.getByText('Execute: Calculator')).toBeInTheDocument()
    })
  })

  it('executes skill successfully', async () => {
    ;(fetch as any)
      .mockResolvedValueOnce({
        json: () => Promise.resolve(mockSkillsResponse),
      })
      .mockResolvedValueOnce({
        json: () => Promise.resolve(mockExecuteResponse),
      })

    const user = userEvent.setup()
    render(<App />)

    const hamburgerBtn = screen.getByLabelText('Toggle skills menu')
    await user.click(hamburgerBtn)

    await waitFor(() => {
      const calculatorSkill = screen.getByText('Calculator')
      fireEvent.click(calculatorSkill)
    })

    const textarea = screen.getByPlaceholderText(/Enter your request for Calculator/)
    await user.type(textarea, 'Calculate 2 + 2')

    const executeBtn = screen.getByText('Execute Skill')
    await user.click(executeBtn)

    await waitFor(() => {
      expect(screen.getByText('Result: 4')).toBeInTheDocument()
    })
  })

  it('handles skill execution error', async () => {
    ;(fetch as any)
      .mockResolvedValueOnce({
        json: () => Promise.resolve(mockSkillsResponse),
      })
      .mockRejectedValueOnce(new Error('Execution failed'))

    const user = userEvent.setup()
    render(<App />)

    const hamburgerBtn = screen.getByLabelText('Toggle skills menu')
    await user.click(hamburgerBtn)

    await waitFor(() => {
      const calculatorSkill = screen.getByText('Calculator')
      fireEvent.click(calculatorSkill)
    })

    const textarea = screen.getByPlaceholderText(/Enter your request for Calculator/)
    await user.type(textarea, 'Calculate 2 + 2')

    const executeBtn = screen.getByText('Execute Skill')
    await user.click(executeBtn)

    await waitFor(() => {
      expect(screen.getByText('Error executing skill')).toBeInTheDocument()
    })
  })
})