import React, {useState} from 'react'

function App(){
  const [goal, setGoal] = useState('')
  const [loading, setLoading] = useState(false)
  const [results, setResults] = useState([])
  const [finalOutput, setFinalOutput] = useState('')

  async function executeGoal(){
    if(!goal.trim()){
      alert('Please enter a goal')
      return
    }
    setLoading(true)
    setResults([])
    setFinalOutput('')

    try{
      const res = await fetch('/api/agent/execute', {
        method: 'POST',
        headers: {'Content-Type':'application/json'},
        body: JSON.stringify({goal})
      })
      if(!res.ok) throw new Error('Network error: ' + res.status)
      const data = await res.json()
      setResults(data.trace || [])
      setFinalOutput(data.finalOutput || '')
    }catch(e){
      setResults([{success:false, skillName:'client', output: e.message}])
    }finally{
      setLoading(false)
    }
  }

  return (
    <div style={{fontFamily: 'Segoe UI, Tahoma, sans-serif', padding: 20, maxWidth: 900, margin: '0 auto'}}>
      <header style={{textAlign:'center', marginBottom:20}}>
        <h1>🤖 Spring AI Agent</h1>
        <p>React UI (minimal scaffold)</p>
      </header>

      <div>
        <label style={{fontWeight:600}}>Enter your goal</label>
        <textarea value={goal} onChange={e=>setGoal(e.target.value)} rows={5} style={{width:'100%', padding:12, marginTop:8}}/>
        <div style={{display:'flex', gap:8, marginTop:12}}>
          <button onClick={executeGoal} disabled={loading} style={{flex:1, padding:12}}>Execute</button>
          <button onClick={()=>{setGoal(''); setResults([]); setFinalOutput('')}} style={{padding:12}}>Clear</button>
        </div>
      </div>

      <div style={{marginTop:20}}>
        {loading && <div>Processing...</div>}
        {results.length>0 && (
          <div>
            <h3>Trace</h3>
            {results.map((r,i)=> (
              <div key={i} style={{padding:10, borderLeft: '4px solid ' + (r.success ? '#2ecc71' : '#e74c3c'), background:'#fff', marginBottom:8}}>
                <div style={{fontWeight:600}}>{r.skillName}</div>
                <div>{r.output}</div>
              </div>
            ))}
          </div>
        )}

        {finalOutput && (
          <div style={{marginTop:12, padding:12, background:'#f6f8fa'}}>
            <strong>Final Output:</strong>
            <div>{finalOutput}</div>
          </div>
        )}
      </div>
    </div>
  )
}

export default App
