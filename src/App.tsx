import { useState, useEffect } from 'react'
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
    <div className="app-layout">
      <header className="app-header">
        <div className="header-brand">
          <div className="brand-copy">
            <div className="brand-mark">AI</div>
            <div>
              <h1>Spring AI Agent</h1>
              <p>Interface</p>
            </div>
          </div>
        </div>
        <div className="header-actions">
          <button 
            className="menu-toggle"
            onClick={() => setIsMenuOpen(!isMenuOpen)}
            aria-label="Toggle skills menu"
          >
            ☰
          </button>
        </div>
      </header>
      
      <div className="app-content">
        <div className={`sidebar-overlay ${isMenuOpen ? 'open' : ''}`} onClick={() => setIsMenuOpen(false)}></div>
        
        <div className="main-view">
          <div style={{ padding: '20px' }}>
            <div className="card">
              <div className="card-header">
                <h2>Counter Demo</h2>
              </div>
              <div className="card-content">
                <button className="btn btn-primary" onClick={() => setCount((count) => count + 1)}>
                  Count is {count}
                </button>
                <p style={{ marginTop: '10px', color: 'var(--text-secondary)' }}>
                  Edit <code>src/App.tsx</code> and save to test HMR
                </p>
              </div>
            </div>

            <div className="card">
              <div className="card-header">
                <h2>Server Connection Test</h2>
              </div>
              <div className="card-content">
                <button className="btn btn-secondary" onClick={fetchMessage} disabled={loading}>
                  {loading ? <span className="loading">Loading...</span> : 'Test Server Connection'}
                </button>
                {message && <p style={{ marginTop: '10px' }}>Server says: {message}</p>}
              </div>
            </div>
          </div>
        </div>

        <div className={`sidebar ${isMenuOpen ? 'open' : ''}`} role="complementary" aria-label="AI Skills Menu">
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
      </div>
    </div>
  )
}

export default App
