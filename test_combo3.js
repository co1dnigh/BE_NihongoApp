const http=require('http'),BASE='http://localhost:8080';
function api(m,p,t,b){
 return new Promise((ok,no)=>{
  const u=new URL(BASE+p),q=http.request({hostname:u.hostname,port:u.port,path:u.pathname,method:m,headers:{'Content-Type':'application/json'}},r=>{
   let d='';r.on('data',c=>d+=c);r.on('end',()=>{try{ok({status:r.statusCode,body:JSON.parse(d)})}catch{ok({status:r.statusCode,body:d})}});
  });q.on('error',no);if(b)q.write(JSON.stringify(b));q.end();
 });
}
(async()=>{
 try{
  const l=await api('POST','/api/v1/auth/login',null,{email:'test45@test.com',password:'Test123456'});
  const tk=l.body.accessToken;
  console.log('login',l.body.user.id);
  const e1=await api('GET','/api/v1/users/me/energy',tk);
  console.log('energy before',JSON.stringify(e1.body));
  const s=await api('POST','/api/v1/lessons/1/start',tk,{});
  const qs=s.body.questions;
  console.log('questions',qs.length);
  // 5 correct, then 1 wrong, then 1 correct
  const answers=qs.map((q,i)=>({questionId:q.questionId,isCorrect: i<5}));
  const sub=await api('POST','/api/v1/lessons/1/submit',tk,{answers,totalQuestions:answers.length,totalCorrect:answers.filter(a=>a.isCorrect).length,totalMistakes:answers.filter(a=>!a.isCorrect).length,isReplay:false});
  console.log('submit status',sub.status,'energy',sub.body.currentEnergy,'exp',sub.body.expEarned);
  const e2=await api('GET','/api/v1/users/me/energy',tk);
  console.log('energy after',JSON.stringify(e2.body));
 }catch(e){console.error('ERR',e.message)}
})();
