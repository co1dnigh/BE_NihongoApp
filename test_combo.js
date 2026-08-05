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
  const e1=await api('GET','/api/v1/users/me/energy',tk);
  const s=await api('POST','/api/v1/lessons/1/start',tk,{});
  const qs=s.body.questions;
  const answers=[{questionId:qs[0].questionId,isCorrect:true},{questionId:qs[1].questionId,isCorrect:true},{questionId:qs[2].questionId,isCorrect:false},{questionId:qs[3].questionId,isCorrect:true},{questionId:qs[4].questionId,isCorrect:true},{questionId:qs[5].questionId,isCorrect:true},{questionId:qs[6].questionId,isCorrect:true}];
  const sub=await api('POST','/api/v1/lessons/1/submit',tk,{answers,totalQuestions:answers.length,totalCorrect:answers.filter(a=>a.isCorrect).length,totalMistakes:answers.filter(a=>!a.isCorrect).length,isReplay:false});
  const e2=await api('GET','/api/v1/users/me/energy',tk);
  console.log(JSON.stringify({login:l.body.user.id,energyBefore:e1.body,questions:qs.length,submit:sub.body,energyAfter:e2.body},null,2));
 }catch(e){console.error('ERR',e.message)}
})();
