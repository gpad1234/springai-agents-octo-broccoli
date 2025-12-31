import { useState, useEffect } from 'react'
import reactLogo from './assets/react.svg'
import viteLogo from '/vite.svg'
import './App.css'

interface Skill {
  name: string;
  description?: string;
}

function App() {
  const [count, setCount] = useState(0)
  const [message, setMessage] = useState('')
  const [loading, setLoading] = useState(false)
  const [skills, setSkills] = useState<Skill[]>([])
  const [isMenuOpen, setIsMenuOpen] = useState(false)
  const [selectedSkill, setSelectedSkill] = useState<string>('')
  const [skillInput, setSkillInput] = useState('')
  const [skillResult, setSkillResult] = useState('')

  useEffect(() => {
    fetchSkills()
  }, [])

  const fetchSkills = async () => {
    try {
      const response = await fetch('http://localhost:8080/api/agent/skills')
      const data = await response.json()
      setSkills(data.skills.map((skill: string) => ({ name: skill })))
    } catch (error) {
      console.error('Failed to fetch skills:', error)
    }
  }

  const fetchMessage = async () => {
    setLoading(true)
    try {
      const response = await fetch('http://localhost:8080/api/agent/message')
      const data = await response.json()
      setMessage(data.message || 'Hello from server!')
    } catch (error) {
      setMessage('Error connecting to server')
    } finally {
      setLoading(false)
    }
  }

  const executeSkill = async () => {
    if (!selectedSkill || !skillInput.trim()) return

    setLoading(true)
    try {
      const response = await fetch('http://localhost:8080/api/agent/execute', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ 
          skill: selectedSkill,
          goal: skillInput 
        }),
      })
      const data = await response.json()
      setSkillResult(data.finalOutput || 'Skill executed successfully!')
    } catch (error) {
      setSkillResult('Error executing skill')
    } finally {
      setLoading(false)
    }
  }

  return (
    <>
      <div>
        <a href="https://vite.dev" target="_blank">
          <img src={viteLogo} className="logo" alt="Vite logo" />
        </a>
        <a href="https://react.dev" target="_blank">
          <img src={reactLogo} className="logo react" alt="React logo" />
        </a>
      </div>
      <h1>Spring AI Agent Interface</h1>
      
      <div className="card">
        <button onClick={() => setCount((count) => count + 1)}>
          count is {count}
        </button>
        <p>
          Edit <code>src/App.tsx</code> and save to test HMR
        </p>
      </div>

      <div className="card">
        <button onClick={fetchMessage} disabled={loading}>
          {loading ? 'Loading...' : 'Test Server Connection'}
        </button>
        {message && <p>Server says: {message}</p>}
      </div>

      {/* Hamburger Menu Button */}
      <button 
        className="hamburger-btn"
        onClick={() => setIsMenuOpen(!isMenuOpen)}
        aria-label="Toggle skills menu"
      >
        <span></span>
        <span></span>
        <span></span>
      </button>

      {/* Sliding Skills Menu */}
      <div className={`skills-menu ${isMenuOpen ? 'open' : ''}`} role="complementary" aria-label="AI Skills Menu">
        <div className="menu-header">
          <h2>AI Skills ({skills.length})</h2>
          <button 
            className="close-btn"
            onClick={() => setIsMenuOpen(false)}
          >
            ×
          </button>
        </div>
        
        <div className="skills-list">
          {skills.map((skill) => (
            <div 
              key={skill.name}
              className={`skill-item ${selectedSkill === skill.name ? 'selected' : ''}`}
              onClick={() => setSelectedSkill(skill.name)}
            >
              <h3>{skill.name.replace('Skill', '')}</h3>
              <p>AI-powered {skill.name.toLowerCase().replace('skill', '')} functionality</p>
            </div>
          ))}
        </div>

        {selectedSkill && (
          <div className="skill-executor">
            <h3>Execute: {selectedSkill.replace('Skill', '')}</h3>
            <div className="skill-instructions">
              <p><strong>Example requests:</strong></p>
              {selectedSkill === 'CalculatorSkill' && (
                <ul>
                  <li>"Calculate 5 * 3"</li>
                  <li>"Calculate 10 + 5"</li>
                  <li>"Calculate (2 + 3) * 4"</li>
                </ul>
              )}
              {selectedSkill === 'WeatherSkill' && (
                <ul>
                  <li>"What's the weather in New York?"</li>
                  <li>"Get weather for London"</li>
                </ul>
              )}
              {selectedSkill === 'SummarizeSkill' && (
                <ul>
                  <li>"Summarize this article: [paste text]"</li>
                  <li>"Give me a summary of: [content]"</li>
                </ul>
              )}
              {selectedSkill === 'MockSearchSkill' && (
                <ul>
                  <li>"Search for information about AI"</li>
                  <li>"Find articles about machine learning"</li>
                </ul>
              )}
              {selectedSkill === 'OsqueryMCPSkill' && (
                <ul>
                  <li>"Run system query"</li>
                  <li>"Check system information"</li>
                </ul>
              )}
            </div>
            <textarea
              placeholder={`Enter your request for ${selectedSkill.replace('Skill', '')}...`}
              value={skillInput}
              onChange={(e) => setSkillInput(e.target.value)}
              rows={3}
            />
            <button 
              onClick={executeSkill} 
              disabled={loading || !skillInput.trim()}
              className="execute-btn"
            >
              {loading ? 'Executing...' : 'Execute Skill'}
            </button>
            {skillResult && (
              <div className="skill-result">
                <h4>Result:</h4>
                <p>{skillResult}</p>
              </div>
            )}
          </div>
        )}
      </div>

      {/* Menu Overlay */}
      {isMenuOpen && <div className="menu-overlay" onClick={() => setIsMenuOpen(false)}></div>}

      <p className="read-the-docs">
        Click on the Vite and React logos to learn more
      </p>
    </>
  )
}

export default App
