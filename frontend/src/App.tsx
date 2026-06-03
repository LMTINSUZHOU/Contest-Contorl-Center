import { type FormEvent, type ReactNode, useEffect, useMemo, useState } from 'react';

const API_BASE = import.meta.env.VITE_API_BASE_URL ?? '';

type Role = 'ADMIN' | 'STUDENT' | 'TEACHER';
type UserView = {
  id: string;
  email: string;
  role: Role;
  roleLabel: string;
  statusLabel: string;
  displayName?: string;
  profileNo?: string;
};
type AwardView = {
  id: string;
  competitionId: string;
  competitionName: string;
  competitionAlias?: string;
  trackId: string;
  trackName?: string;
  competitionGradeLabel: string;
  awardLevel: string;
  awardLevelLabel: string;
  subjectType: 'STUDENT' | 'TEAM' | 'TEACHER';
  subjectTypeLabel: string;
  awardDate?: string;
  awardLocation?: string;
  primaryStudentId?: string;
  studentName?: string;
  teacherSubjectId?: string;
  teacherName?: string;
  teamId?: string;
  teamName?: string;
  advisorTeacherIds: string[];
  advisorNames: string[];
  auditStatus: 'PENDING' | 'APPROVED' | 'REJECTED';
  auditStatusLabel: string;
  auditOpinion?: string;
  certificateId?: string;
};
type Entity = Record<string, any>;
type SelectOption = { value: string; label: string; keywords?: string };

function token() {
  return localStorage.getItem('contest-token');
}

async function api<T>(path: string, options: RequestInit = {}): Promise<T> {
  const headers = new Headers(options.headers);
  if (!(options.body instanceof FormData)) {
    headers.set('Content-Type', 'application/json');
  }
  if (token()) {
    headers.set('Authorization', `Bearer ${token()}`);
  }
  const response = await fetch(`${API_BASE}${path}`, { ...options, headers });
  if (!response.ok) {
    const payload = await response.json().catch(() => ({ message: response.statusText }));
    throw new Error(payload.message ?? response.statusText);
  }
  if (response.status === 204) {
    return undefined as T;
  }
  return response.json() as Promise<T>;
}

async function downloadApi(path: string, filename: string) {
  const headers = new Headers();
  if (token()) {
    headers.set('Authorization', `Bearer ${token()}`);
  }
  const response = await fetch(`${API_BASE}${path}`, { headers });
  if (!response.ok) {
    const payload = await response.json().catch(() => ({ message: response.statusText }));
    throw new Error(payload.message ?? response.statusText);
  }
  const blob = await response.blob();
  const url = URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = url;
  link.download = filename;
  link.click();
  URL.revokeObjectURL(url);
}

function App() {
  const [user, setUser] = useState<UserView | null>(null);
  const [message, setMessage] = useState('');

  useEffect(() => {
    if (!token()) return;
    api<UserView>('/api/auth/me')
      .then(setUser)
      .catch(() => localStorage.removeItem('contest-token'));
  }, []);

  const logout = () => {
    localStorage.removeItem('contest-token');
    setUser(null);
  };

  if (!user) {
    return <AuthPage onLogin={setUser} setMessage={setMessage} message={message} />;
  }

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <div className="brand">学科竞赛管理系统</div>
        <div className="user-block">
          <strong>{user.displayName || user.email}</strong>
          <span>{user.roleLabel} · {user.statusLabel}</span>
        </div>
        <button className="ghost" onClick={logout}>退出登录</button>
      </aside>
      <main className="workspace">
        {user.role === 'ADMIN' && <AdminWorkspace />}
        {user.role === 'STUDENT' && <StudentWorkspace />}
        {user.role === 'TEACHER' && <TeacherWorkspace />}
      </main>
    </div>
  );
}

function AuthPage({ onLogin, message, setMessage }: { onLogin: (user: UserView) => void; message: string; setMessage: (value: string) => void }) {
  const [mode, setMode] = useState<'login' | 'student' | 'teacher'>('login');
  const [form, setForm] = useState<Entity>({ email: 'admin@contest.local', password: 'Admin@123456' });

  const submit = async (event: FormEvent) => {
    event.preventDefault();
    setMessage('');
    try {
      if (mode === 'login') {
        const result = await api<{ token: string; user: UserView }>('/api/auth/login', {
          method: 'POST',
          body: JSON.stringify(form)
        });
        localStorage.setItem('contest-token', result.token);
        onLogin(result.user);
      } else {
        const path = mode === 'student' ? '/api/auth/register/student' : '/api/auth/register/teacher';
        const result = await api<UserView>(path, { method: 'POST', body: JSON.stringify(form) });
        setMessage(`注册已提交：${result.email}，请等待管理员审核。`);
        setMode('login');
      }
    } catch (error) {
      setMessage((error as Error).message);
    }
  };

  return (
    <div className="auth-layout">
      <section className="auth-panel">
        <h1>学科竞赛管理系统</h1>
        <div className="segmented">
          <button className={mode === 'login' ? 'active' : ''} onClick={() => setMode('login')}>登录</button>
          <button className={mode === 'student' ? 'active' : ''} onClick={() => setMode('student')}>学生注册</button>
          <button className={mode === 'teacher' ? 'active' : ''} onClick={() => setMode('teacher')}>教师注册</button>
        </div>
        <form onSubmit={submit} className="form-grid">
          <TextInput label="邮箱" value={form.email} onChange={email => setForm({ ...form, email })} />
          <TextInput label="密码" type="password" value={form.password} onChange={password => setForm({ ...form, password })} />
          {mode === 'student' && (
            <>
              <TextInput label="学号" value={form.studentNo} onChange={studentNo => setForm({ ...form, studentNo })} />
              <TextInput label="姓名" value={form.name} onChange={name => setForm({ ...form, name })} />
              <TextInput label="学院" value={form.college} onChange={college => setForm({ ...form, college })} />
              <TextInput label="专业" value={form.major} onChange={major => setForm({ ...form, major })} />
            </>
          )}
          {mode === 'teacher' && (
            <>
              <TextInput label="工号" value={form.teacherNo} onChange={teacherNo => setForm({ ...form, teacherNo })} />
              <TextInput label="姓名" value={form.name} onChange={name => setForm({ ...form, name })} />
              <TextInput label="学院" value={form.college} onChange={college => setForm({ ...form, college })} />
              <TextInput label="职称" value={form.title} onChange={title => setForm({ ...form, title })} />
            </>
          )}
          <button className="primary" type="submit">{mode === 'login' ? '登录' : '提交注册'}</button>
        </form>
        {message && <p className="message">{message}</p>}
      </section>
    </div>
  );
}

function AdminWorkspace() {
  const tabs = ['首页', '用户审核', '学生', '教师', '竞赛', '团队', '获奖', '导入导出'];
  const [tab, setTab] = useState(tabs[0]);
  return (
    <>
      <Header title="后台管理" tabs={tabs} active={tab} onChange={setTab} />
      {tab === '首页' && <Dashboard />}
      {tab === '用户审核' && <PendingUsers />}
      {tab === '学生' && <ProfileManager type="students" />}
      {tab === '教师' && <ProfileManager type="teachers" />}
      {tab === '竞赛' && <CompetitionManager />}
      {tab === '团队' && <TeamManager />}
      {tab === '获奖' && <AwardManager />}
      {tab === '导入导出' && <ImportExportPanel />}
    </>
  );
}

function Dashboard() {
  const [summary, setSummary] = useState<Entity | null>(null);
  const [stats, setStats] = useState<Entity>({});
  useEffect(() => {
    api<Entity>('/api/admin/dashboard/summary').then(setSummary);
    api<Entity>('/api/admin/statistics/awards?dimension=year').then(setStats);
  }, []);
  if (!summary) return <Loading />;
  return (
    <section>
      <div className="metric-grid">
        {Object.entries(summary).map(([key, value]) => <div className="metric" key={key}><span>{labelOf(key)}</span><strong>{String(value)}</strong></div>)}
      </div>
      <h2>按年份统计</h2>
      <SimpleTable rows={Object.entries(stats).map(([name, count]) => ({ name, count }))} />
    </section>
  );
}

function PendingUsers() {
  const [rows, setRows] = useState<UserView[]>([]);
  const [message, setMessage] = useState('');
  const load = () => api<UserView[]>('/api/admin/users/pending').then(setRows);
  useEffect(() => { load(); }, []);
  const audit = async (id: string, action: 'approve' | 'reject') => {
    await api(`/api/admin/users/${id}/${action}`, { method: 'POST', body: JSON.stringify({ reason: action === 'approve' ? '审核通过' : '资料需补充' }) });
    setMessage('处理完成');
    load();
  };
  return (
    <section>
      {message && <p className="message">{message}</p>}
      <SimpleTable rows={rows} actions={row => (
        <>
          <button onClick={() => audit(row.id, 'approve')}>通过</button>
          <button className="danger" onClick={() => audit(row.id, 'reject')}>驳回</button>
        </>
      )} />
    </section>
  );
}

function ProfileManager({ type }: { type: 'students' | 'teachers' }) {
  const [rows, setRows] = useState<Entity[]>([]);
  const [form, setForm] = useState<Entity>({});
  const [resetForm, setResetForm] = useState<Entity>({});
  const [message, setMessage] = useState('');
  const path = `/api/admin/${type}`;
  const isStudent = type === 'students';
  const load = () => api<Entity[]>(path).then(setRows);
  useEffect(() => { load(); }, [path]);
  const save = async (event: FormEvent) => {
    event.preventDefault();
    await api(path, { method: 'POST', body: JSON.stringify(form) });
    setForm({});
    load();
  };
  const resetPassword = async (event: FormEvent) => {
    event.preventDefault();
    if (!resetForm.profileId || !resetForm.password) return;
    await api(`${path}/${resetForm.profileId}/password`, { method: 'POST', body: JSON.stringify({ password: resetForm.password }) });
    setResetForm({});
    setMessage('密码已重置');
  };
  return (
    <section className="two-column">
      <form className="panel form-grid" onSubmit={save}>
        <h2>{isStudent ? '新增学生' : '新增教师'}</h2>
        <TextInput label="邮箱" value={form.email} onChange={email => setForm({ ...form, email })} />
        <TextInput label={isStudent ? '学号' : '工号'} value={isStudent ? form.studentNo : form.teacherNo} onChange={value => setForm({ ...form, [isStudent ? 'studentNo' : 'teacherNo']: value })} />
        <TextInput label="姓名" value={form.name} onChange={name => setForm({ ...form, name })} />
        <TextInput label="学院" value={form.college} onChange={college => setForm({ ...form, college })} />
        {isStudent ? <TextInput label="专业" value={form.major} onChange={major => setForm({ ...form, major })} /> : <TextInput label="职称" value={form.title} onChange={title => setForm({ ...form, title })} />}
        <button className="primary">保存</button>
      </form>
      <div>
        {message && <p className="message">{message}</p>}
        <form className="panel inline-form" onSubmit={resetPassword}>
          <SelectInput
            label={isStudent ? '学生' : '教师'}
            value={resetForm.profileId}
            searchable
            options={rows.map(row => ({ value: row.id, label: `${row.name}(${isStudent ? row.studentNo : row.teacherNo})`, keywords: `${row.name} ${isStudent ? row.studentNo : row.teacherNo}` }))}
            onChange={profileId => setResetForm({ ...resetForm, profileId })}
          />
          <TextInput label="新密码" type="password" value={resetForm.password} onChange={password => setResetForm({ ...resetForm, password })} />
          <button>重置密码</button>
        </form>
        <SimpleTable rows={rows} actions={row => <button onClick={() => setResetForm({ profileId: row.id, password: '' })}>选择重置</button>} />
      </div>
    </section>
  );
}

function CompetitionManager() {
  const [competitions, setCompetitions] = useState<Entity[]>([]);
  const [tracks, setTracks] = useState<Entity[]>([]);
  const [competitionForm, setCompetitionForm] = useState<Entity>({ defaultGrade: 'OTHER', enabled: true });
  const [trackForm, setTrackForm] = useState<Entity>({});
  const [editingCompetitionId, setEditingCompetitionId] = useState<string | null>(null);
  const [editingTrackId, setEditingTrackId] = useState<string | null>(null);
  const [competitionFormOpen, setCompetitionFormOpen] = useState(false);
  const [trackFormOpen, setTrackFormOpen] = useState(false);
  const load = () => {
    api<Entity[]>('/api/admin/competitions').then(setCompetitions);
    api<Entity[]>('/api/admin/competition-tracks').then(setTracks);
  };
  useEffect(() => { load(); }, []);
  const saveCompetition = async (event: FormEvent) => {
    event.preventDefault();
    await api(editingCompetitionId ? `/api/admin/competitions/${editingCompetitionId}` : '/api/admin/competitions', {
      method: editingCompetitionId ? 'PUT' : 'POST',
      body: JSON.stringify(competitionForm)
    });
    setCompetitionForm({ defaultGrade: 'OTHER', enabled: true });
    setEditingCompetitionId(null);
    setCompetitionFormOpen(false);
    load();
  };
  const saveTrack = async (event: FormEvent) => {
    event.preventDefault();
    await api(editingTrackId ? `/api/admin/competition-tracks/${editingTrackId}` : '/api/admin/competition-tracks', {
      method: editingTrackId ? 'PUT' : 'POST',
      body: JSON.stringify({ ...trackForm, enabled: true })
    });
    setTrackForm({});
    setEditingTrackId(null);
    setTrackFormOpen(false);
    load();
  };
  const remove = async (path: string) => {
    await api(path, { method: 'DELETE' });
    load();
  };
  const openCompetitionForm = (row?: Entity) => {
    setEditingCompetitionId(row?.id ?? null);
    setCompetitionForm(row ? { ...row } : { defaultGrade: 'OTHER', enabled: true });
    setCompetitionFormOpen(true);
  };
  const openTrackForm = (row?: Entity) => {
    setEditingTrackId(row?.id ?? null);
    setTrackForm(row ? { ...row } : {});
    setTrackFormOpen(true);
  };
  const closeCompetitionForm = () => {
    setEditingCompetitionId(null);
    setCompetitionForm({ defaultGrade: 'OTHER', enabled: true });
    setCompetitionFormOpen(false);
  };
  const closeTrackForm = () => {
    setEditingTrackId(null);
    setTrackForm({});
    setTrackFormOpen(false);
  };
  return (
    <section>
      <div className="panel button-row">
        <button type="button" className="primary" onClick={() => openCompetitionForm()}>新增竞赛</button>
        <button type="button" onClick={() => openTrackForm()}>新增赛道</button>
      </div>
      <Modal title={editingCompetitionId ? '编辑竞赛' : '新增竞赛'} open={competitionFormOpen} onClose={closeCompetitionForm}>
        <form className="inline-form modal-form" onSubmit={saveCompetition}>
          <TextInput label="竞赛名称" value={competitionForm.name} onChange={name => setCompetitionForm({ ...competitionForm, name })} />
          <SelectInput label="等级" value={competitionForm.defaultGrade} options={gradeOptions()} onChange={defaultGrade => setCompetitionForm({ ...competitionForm, defaultGrade })} />
          <TextInput label="主办单位" value={competitionForm.organizer} onChange={organizer => setCompetitionForm({ ...competitionForm, organizer })} />
          <div className="form-actions">
            <button className="primary">{editingCompetitionId ? '保存竞赛' : '新增竞赛'}</button>
            <button type="button" onClick={closeCompetitionForm}>取消</button>
          </div>
        </form>
      </Modal>
      <Modal title={editingTrackId ? '编辑赛道' : '新增赛道'} open={trackFormOpen} onClose={closeTrackForm}>
        <form className="inline-form modal-form" onSubmit={saveTrack}>
          <SelectInput label="所属竞赛" value={trackForm.competitionId} searchable options={competitions.map(c => ({ value: c.id, label: c.name, keywords: c.name }))} onChange={competitionId => setTrackForm({ ...trackForm, competitionId })} />
          <TextInput label="赛道名称" value={trackForm.name} onChange={name => setTrackForm({ ...trackForm, name })} />
          <div className="form-actions">
            <button className="primary">{editingTrackId ? '保存赛道' : '新增赛道'}</button>
            <button type="button" onClick={closeTrackForm}>取消</button>
          </div>
        </form>
      </Modal>
      <SimpleTable rows={competitions} title="竞赛" actions={row => (
        <>
          <button onClick={() => openCompetitionForm(row)}>编辑</button>
          <button className="danger" onClick={() => remove(`/api/admin/competitions/${row.id}`)}>删除</button>
        </>
      )} />
      <SimpleTable rows={tracks} title="赛道" actions={row => (
        <>
          <button onClick={() => openTrackForm(row)}>编辑</button>
          <button className="danger" onClick={() => remove(`/api/admin/competition-tracks/${row.id}`)}>删除</button>
        </>
      )} />
    </section>
  );
}

function TeamManager() {
  const [teams, setTeams] = useState<Entity[]>([]);
  const [students, setStudents] = useState<Entity[]>([]);
  const [form, setForm] = useState<Entity>({});
  const [formOpen, setFormOpen] = useState(false);
  const load = () => {
    api<Entity[]>('/api/admin/teams').then(setTeams);
    api<Entity[]>('/api/admin/students').then(setStudents);
  };
  useEffect(() => { load(); }, []);
  const save = async (event: FormEvent) => {
    event.preventDefault();
    await api('/api/admin/teams', {
      method: 'POST',
      body: JSON.stringify({ ...form, memberStudentIds: form.memberStudentIds || [] })
    });
    setForm({});
    setFormOpen(false);
    load();
  };
  const closeForm = () => {
    setForm({});
    setFormOpen(false);
  };
  return (
    <section>
      <div className="panel button-row">
        <button type="button" className="primary" onClick={() => setFormOpen(true)}>新增团队</button>
      </div>
      <Modal title="新增团队" open={formOpen} onClose={closeForm}>
        <form className="form-grid modal-form" onSubmit={save}>
          <TextInput label="团队名称" value={form.name} onChange={name => setForm({ ...form, name })} />
          <SelectInput label="队长" value={form.captainStudentId} searchable options={students.map(s => ({ value: s.id, label: `${s.name}(${s.studentNo})`, keywords: `${s.name} ${s.studentNo}` }))} onChange={captainStudentId => setForm({ ...form, captainStudentId })} />
          <MultiSelect label="团队成员" value={form.memberStudentIds || []} searchable options={students.map(s => ({ value: s.id, label: `${s.name}(${s.studentNo})`, keywords: `${s.name} ${s.studentNo}` }))} onChange={memberStudentIds => setForm({ ...form, memberStudentIds })} />
          <div className="form-actions">
            <button className="primary">保存</button>
            <button type="button" onClick={closeForm}>取消</button>
          </div>
        </form>
      </Modal>
      <SimpleTable rows={teams} />
    </section>
  );
}

function AwardManager() {
  const [awards, setAwards] = useState<AwardView[]>([]);
  const [competitions, setCompetitions] = useState<Entity[]>([]);
  const [tracks, setTracks] = useState<Entity[]>([]);
  const [students, setStudents] = useState<Entity[]>([]);
  const [teachers, setTeachers] = useState<Entity[]>([]);
  const [teams, setTeams] = useState<Entity[]>([]);
  const [form, setForm] = useState<Entity>({ subjectType: 'STUDENT', awardLevel: 'FIRST_PRIZE' });
  const [editingId, setEditingId] = useState<string | null>(null);
  const [formOpen, setFormOpen] = useState(false);
  const [message, setMessage] = useState('');
  const load = () => {
    api<AwardView[]>('/api/admin/awards').then(setAwards);
    api<Entity[]>('/api/admin/competitions').then(setCompetitions);
    api<Entity[]>('/api/admin/students').then(setStudents);
    api<Entity[]>('/api/admin/teachers').then(setTeachers);
    api<Entity[]>('/api/admin/teams').then(setTeams);
  };
  useEffect(() => { load(); }, []);
  const loadTracks = (competitionId: string) => {
    if (!competitionId) {
      setTracks([]);
      return;
    }
    api<Entity[]>(`/api/catalog/tracks?competitionId=${competitionId}`).then(setTracks);
  };
  const save = async (event: FormEvent) => {
    event.preventDefault();
    await api(editingId ? `/api/admin/awards/${editingId}` : '/api/admin/awards', {
      method: editingId ? 'PUT' : 'POST',
      body: JSON.stringify({ ...form, auditStatus: 'APPROVED' })
    });
    setForm({ subjectType: 'STUDENT', awardLevel: 'FIRST_PRIZE' });
    setEditingId(null);
    setTracks([]);
    setFormOpen(false);
    load();
  };
  const editAward = (row: AwardView) => {
    setEditingId(row.id);
    setForm({
      competitionId: row.competitionId,
      trackId: row.trackId,
      competitionAlias: row.competitionAlias || '',
      subjectType: row.subjectType,
      awardLevel: row.awardLevel,
      primaryStudentId: row.primaryStudentId || null,
      teamId: row.teamId || null,
      awardDate: row.awardDate || '',
      awardLocation: row.awardLocation || '',
      advisorTeacherIds: row.advisorTeacherIds || []
    });
    loadTracks(row.competitionId);
    setFormOpen(true);
  };
  const deleteAward = async (id: string) => {
    await api(`/api/admin/awards/${id}`, { method: 'DELETE' });
    setMessage('获奖记录已删除');
    if (editingId === id) {
      setEditingId(null);
      setForm({ subjectType: 'STUDENT', awardLevel: 'FIRST_PRIZE' });
      setFormOpen(false);
    }
    load();
  };
  const audit = async (id: string, action: 'approve' | 'reject') => {
    await api(`/api/admin/award-declarations/${id}/${action}`, { method: 'POST', body: JSON.stringify({ reason: action === 'approve' ? '审核通过' : '资料不完整' }) });
    load();
  };
  const uploadCertificate = async (awardId: string, file?: File) => {
    if (!file) return;
    const payload = new FormData();
    payload.append('file', file);
    await api(`/api/certificates/awards/${awardId}`, { method: 'POST', body: payload });
    setMessage('证书已导入');
    load();
  };
  const openCreateForm = () => {
    setEditingId(null);
    setForm({ subjectType: 'STUDENT', awardLevel: 'FIRST_PRIZE' });
    setTracks([]);
    setFormOpen(true);
  };
  const closeForm = () => {
    setEditingId(null);
    setForm({ subjectType: 'STUDENT', awardLevel: 'FIRST_PRIZE' });
    setTracks([]);
    setFormOpen(false);
  };
  return (
    <section>
      {message && <p className="message">{message}</p>}
      <div className="panel button-row">
        <button type="button" className="primary" onClick={openCreateForm}>录入获奖</button>
      </div>
      <Modal title={editingId ? '编辑获奖' : '录入获奖'} open={formOpen} onClose={closeForm}>
        <form className="inline-form modal-form" onSubmit={save}>
          <SelectInput label="竞赛" value={form.competitionId} searchable options={competitions.map(c => ({ value: c.id, label: c.name, keywords: c.name }))} onChange={competitionId => { setForm({ ...form, competitionId, trackId: null }); loadTracks(competitionId); }} />
          <SelectInput label="赛道" value={form.trackId} searchable options={tracks.map(track => ({ value: track.id, label: track.name, keywords: track.name }))} onChange={trackId => setForm({ ...form, trackId })} />
          <TextInput label="竞赛别名" value={form.competitionAlias} onChange={competitionAlias => setForm({ ...form, competitionAlias })} />
          <SelectInput label="主体" value={form.subjectType} options={subjectOptions()} onChange={subjectType => setForm({ ...form, subjectType, primaryStudentId: null, teamId: null })} />
          <SelectInput label="获奖等级" value={form.awardLevel} options={awardOptions()} onChange={awardLevel => setForm({ ...form, awardLevel })} />
          {form.subjectType === 'TEAM'
            ? <SelectInput label="团队" value={form.teamId} searchable options={teams.map(t => ({ value: t.id, label: t.name, keywords: t.name }))} onChange={teamId => setForm({ ...form, teamId })} />
            : <SelectInput label="学生" value={form.primaryStudentId} searchable options={students.map(s => ({ value: s.id, label: `${s.name}(${s.studentNo})`, keywords: `${s.name} ${s.studentNo}` }))} onChange={primaryStudentId => setForm({ ...form, primaryStudentId })} />}
          <MultiSelect label="指导老师" value={form.advisorTeacherIds || []} searchable options={teachers.map(t => ({ value: t.id, label: `${t.name}(${t.teacherNo})`, keywords: `${t.name} ${t.teacherNo}` }))} onChange={advisorTeacherIds => setForm({ ...form, advisorTeacherIds })} />
          <TextInput label="获奖日期" type="date" value={form.awardDate} onChange={awardDate => setForm({ ...form, awardDate })} />
          <TextInput label="获奖地点" value={form.awardLocation} onChange={awardLocation => setForm({ ...form, awardLocation })} />
          <div className="form-actions">
            <button className="primary">{editingId ? '保存修改' : '录入获奖'}</button>
            <button type="button" onClick={closeForm}>取消</button>
          </div>
        </form>
      </Modal>
      <AwardTable rows={awards} onCertificateUpload={uploadCertificate} actions={row => (
        <>
          <button onClick={() => editAward(row)}>编辑</button>
          <button className="danger" onClick={() => deleteAward(row.id)}>删除</button>
          {row.auditStatusLabel === '待审核' && <button onClick={() => audit(row.id, 'approve')}>通过</button>}
          {row.auditStatusLabel === '待审核' && <button className="danger" onClick={() => audit(row.id, 'reject')}>驳回</button>}
        </>
      )} />
    </section>
  );
}

function ImportExportPanel() {
  const types = ['students', 'teachers', 'competitions', 'awards'];
  const [type, setType] = useState(types[0]);
  const [message, setMessage] = useState('');
  const upload = async (file?: File) => {
    if (!file) return;
    const form = new FormData();
    form.append('file', file);
    const result = await api<Entity>(`/api/admin/import-export/${type}/import`, { method: 'POST', body: form });
    setMessage(`导入完成：成功 ${result.successRows} 行，错误 ${result.errorRows} 行，任务 ${result.jobId}`);
  };
  return (
    <section className="panel form-grid">
      <SelectInput label="数据类型" value={type} options={types.map(value => ({ value, label: value }))} onChange={setType} />
      <div className="button-row">
        <button onClick={() => downloadApi(`/api/admin/import-export/${type}/template`, `${type}-template.xlsx`)}>下载模板</button>
        <button onClick={() => downloadApi(`/api/admin/import-export/${type}/export`, `${type}-export.xlsx`)}>导出数据</button>
      </div>
      <input type="file" accept=".xlsx" onChange={event => upload(event.target.files?.[0])} />
      {message && <p className="message">{message}</p>}
    </section>
  );
}

function StudentWorkspace() {
  const [profile, setProfile] = useState<Entity | null>(null);
  const [awards, setAwards] = useState<AwardView[]>([]);
  const [declarations, setDeclarations] = useState<AwardView[]>([]);
  const [competitions, setCompetitions] = useState<Entity[]>([]);
  const [tracks, setTracks] = useState<Entity[]>([]);
  const [teachers, setTeachers] = useState<Entity[]>([]);
  const [teams, setTeams] = useState<Entity[]>([]);
  const [form, setForm] = useState<Entity>({ awardLevel: 'FIRST_PRIZE', teamAward: false });
  const [editingId, setEditingId] = useState<string | null>(null);
  const [formOpen, setFormOpen] = useState(false);
  const [message, setMessage] = useState('');
  const load = () => {
    api<Entity>('/api/student/profile').then(setProfile);
    api<AwardView[]>('/api/student/awards').then(setAwards);
    api<AwardView[]>('/api/student/award-declarations').then(setDeclarations);
    api<Entity[]>('/api/catalog/competitions').then(setCompetitions);
    api<Entity[]>('/api/catalog/teachers').then(setTeachers);
    api<Entity[]>('/api/catalog/teams').then(setTeams);
  };
  useEffect(() => { load(); }, []);
  const loadTracks = (competitionId: string) => {
    if (!competitionId) {
      setTracks([]);
      return;
    }
    api<Entity[]>(`/api/catalog/tracks?competitionId=${competitionId}`).then(setTracks);
  };
  const submit = async (event: FormEvent) => {
    event.preventDefault();
    await api(editingId ? `/api/student/award-declarations/${editingId}` : '/api/student/award-declarations', {
      method: editingId ? 'PUT' : 'POST',
      body: JSON.stringify(form)
    });
    setForm({ awardLevel: 'FIRST_PRIZE', teamAward: false });
    setEditingId(null);
    setTracks([]);
    setFormOpen(false);
    setMessage(editingId ? '更新已提交，等待审核' : '申报已提交，等待审核');
    load();
  };
  const editDeclaration = (row: AwardView) => {
    setEditingId(row.id);
    setForm({
      competitionId: row.competitionId,
      trackId: row.trackId,
      competitionAlias: row.competitionAlias || '',
      awardLevel: row.awardLevel,
      teamId: row.teamId || null,
      teamAward: Boolean(row.teamId),
      awardDate: row.awardDate || '',
      awardLocation: row.awardLocation || '',
      advisorTeacherIds: row.advisorTeacherIds || []
    });
    loadTracks(row.competitionId);
    setFormOpen(true);
  };
  const deleteDeclaration = async (id: string) => {
    await api(`/api/student/award-declarations/${id}`, { method: 'DELETE' });
    setMessage('记录已删除');
    if (editingId === id) {
      setEditingId(null);
      setForm({ awardLevel: 'FIRST_PRIZE', teamAward: false });
      setFormOpen(false);
    }
    load();
  };
  const openCreateForm = () => {
    setEditingId(null);
    setForm({ awardLevel: 'FIRST_PRIZE', teamAward: false });
    setTracks([]);
    setFormOpen(true);
  };
  const closeForm = () => {
    setEditingId(null);
    setForm({ awardLevel: 'FIRST_PRIZE', teamAward: false });
    setTracks([]);
    setFormOpen(false);
  };
  return (
    <>
      <Header title="学生主页" />
      {message && <p className="message">{message}</p>}
      <section className="panel"><strong>{profile?.name}</strong> {profile?.studentNo} {profile?.college} {profile?.major}</section>
      <section className="panel button-row">
        <button type="button" className="primary" onClick={openCreateForm}>提交获奖申报</button>
      </section>
      <Modal title={editingId ? '编辑申报' : '提交获奖申报'} open={formOpen} onClose={closeForm}>
        <form className="inline-form modal-form" onSubmit={submit}>
          <SelectInput label="竞赛" value={form.competitionId} searchable options={competitions.map(c => ({ value: c.id, label: c.name, keywords: c.name }))} onChange={competitionId => { setForm({ ...form, competitionId, trackId: null }); loadTracks(competitionId); }} />
          <SelectInput label="赛道" value={form.trackId} searchable options={tracks.map(track => ({ value: track.id, label: track.name, keywords: track.name }))} onChange={trackId => setForm({ ...form, trackId })} />
          <TextInput label="竞赛别名" value={form.competitionAlias} onChange={competitionAlias => setForm({ ...form, competitionAlias })} />
          <SelectInput label="获奖等级" value={form.awardLevel} options={awardOptions()} onChange={awardLevel => setForm({ ...form, awardLevel })} />
          <SelectInput label="团队" value={form.teamId} searchable options={[{ value: '', label: '个人赛', keywords: '个人赛' }, ...teams.map(t => ({ value: t.id, label: t.name, keywords: t.name }))]} onChange={teamId => setForm({ ...form, teamId: teamId || null, teamAward: Boolean(teamId) })} />
          <MultiSelect label="指导老师" value={form.advisorTeacherIds || []} searchable options={teachers.map(t => ({ value: t.id, label: `${t.name}(${t.teacherNo})`, keywords: `${t.name} ${t.teacherNo}` }))} onChange={advisorTeacherIds => setForm({ ...form, advisorTeacherIds })} />
          <TextInput label="获奖日期" type="date" value={form.awardDate} onChange={awardDate => setForm({ ...form, awardDate })} />
          <TextInput label="获奖地点" value={form.awardLocation} onChange={awardLocation => setForm({ ...form, awardLocation })} />
          <div className="form-actions">
            <button className="primary">{editingId ? '提交修改' : '提交申报'}</button>
            <button type="button" onClick={closeForm}>取消</button>
          </div>
        </form>
      </Modal>
      <h2>历史获奖</h2>
      <AwardTable rows={awards} />
      <h2>申报/可维护记录</h2>
      <AwardTable rows={declarations} actions={row => (
        <>
          <button onClick={() => editDeclaration(row)}>编辑</button>
          <button className="danger" onClick={() => deleteDeclaration(row.id)}>删除</button>
        </>
      )} />
    </>
  );
}

function TeacherWorkspace() {
  const [profile, setProfile] = useState<Entity | null>(null);
  const [awards, setAwards] = useState<AwardView[]>([]);
  const [declarations, setDeclarations] = useState<AwardView[]>([]);
  const load = () => {
    api<Entity>('/api/teacher/profile').then(setProfile);
    api<AwardView[]>('/api/teacher/awards').then(setAwards);
    api<AwardView[]>('/api/teacher/award-declarations').then(setDeclarations);
  };
  useEffect(() => {
    load();
  }, []);
  const audit = async (id: string, action: 'approve' | 'reject') => {
    await api(`/api/teacher/award-declarations/${id}/${action}`, { method: 'POST', body: JSON.stringify({ reason: action === 'approve' ? '指导老师审核通过' : '指导老师驳回' }) });
    load();
  };
  return (
    <>
      <Header title="指导老师主页" />
      <section className="panel"><strong>{profile?.name}</strong> {profile?.teacherNo} {profile?.college} {profile?.title}</section>
      <h2>待审核申报</h2>
      <AwardTable rows={declarations} actions={row => (
        <>
          <button onClick={() => audit(row.id, 'approve')}>通过</button>
          <button className="danger" onClick={() => audit(row.id, 'reject')}>驳回</button>
        </>
      )} />
      <h2>指导与获奖记录</h2>
      <AwardTable rows={awards} />
    </>
  );
}

function Header({ title, tabs, active, onChange }: { title: string; tabs?: string[]; active?: string; onChange?: (value: string) => void }) {
  return (
    <header className="page-header">
      <h1>{title}</h1>
      {tabs && <nav className="tabs">{tabs.map(tab => <button key={tab} className={tab === active ? 'active' : ''} onClick={() => onChange?.(tab)}>{tab}</button>)}</nav>}
    </header>
  );
}

function TextInput({ label, value, onChange, type = 'text' }: { label: string; value?: any; onChange: (value: string) => void; type?: string }) {
  return <label><span>{label}</span><input type={type} value={value ?? ''} onChange={event => onChange(event.target.value)} /></label>;
}

function SelectInput({ label, value, options, onChange, searchable = false }: { label: string; value?: any; options: SelectOption[]; onChange: (value: string) => void; searchable?: boolean }) {
  const [open, setOpen] = useState(false);
  const [query, setQuery] = useState('');
  const normalizedValue = String(value ?? '');
  const selected = options.find(option => option.value === normalizedValue);
  const hasEmptyOption = options.some(option => option.value === '');
  const canSearch = searchable || options.length > 8;
  const filteredOptions = useMemo(() => filterOptions(options, query), [options, query]);

  const choose = (nextValue: string) => {
    onChange(nextValue);
    setOpen(false);
    setQuery('');
  };

  return (
    <div className="select-field">
      <span className="field-label">{label}</span>
      <button type="button" className="select-trigger" aria-expanded={open} onClick={() => setOpen(current => !current)}>
        <span className={selected ? '' : 'select-placeholder'}>{selected?.label ?? '请选择'}</span>
        <span className="select-caret">v</span>
      </button>
      {open && (
        <div className="select-menu">
          {canSearch && <input className="select-search" placeholder="搜索" value={query} onChange={event => setQuery(event.target.value)} />}
          {!hasEmptyOption && (
            <label className={`select-option ${normalizedValue === '' ? 'selected' : ''}`}>
              <input type="checkbox" checked={normalizedValue === ''} onChange={() => choose('')} />
              <span>请选择</span>
            </label>
          )}
          {filteredOptions.map(option => (
            <label className={`select-option ${option.value === normalizedValue ? 'selected' : ''}`} key={option.value}>
              <input type="checkbox" checked={option.value === normalizedValue} onChange={() => choose(option.value)} />
              <span>{option.label}</span>
            </label>
          ))}
          {filteredOptions.length === 0 && <div className="select-empty">无匹配选项</div>}
        </div>
      )}
    </div>
  );
}

function MultiSelect({ label, value, options, onChange, searchable = false }: { label: string; value: string[]; options: SelectOption[]; onChange: (value: string[]) => void; searchable?: boolean }) {
  const [open, setOpen] = useState(false);
  const [query, setQuery] = useState('');
  const selectedValues = new Set(value || []);
  const selectedLabels = options.filter(option => selectedValues.has(option.value)).map(option => option.label);
  const canSearch = searchable || options.length > 8;
  const filteredOptions = useMemo(() => filterOptions(options, query), [options, query]);

  const toggle = (optionValue: string) => {
    const next = new Set(selectedValues);
    if (next.has(optionValue)) {
      next.delete(optionValue);
    } else {
      next.add(optionValue);
    }
    onChange(Array.from(next));
  };

  return (
    <div className="select-field">
      <span className="field-label">{label}</span>
      <button type="button" className="select-trigger" aria-expanded={open} onClick={() => setOpen(current => !current)}>
        <span className={selectedLabels.length ? '' : 'select-placeholder'}>{selectedLabels.length ? selectedLabels.join('，') : '请选择'}</span>
        <span className="select-caret">v</span>
      </button>
      {open && (
        <div className="select-menu">
          {canSearch && <input className="select-search" placeholder="搜索" value={query} onChange={event => setQuery(event.target.value)} />}
          {value?.length > 0 && <button type="button" className="select-clear" onClick={() => onChange([])}>清空已选</button>}
          {filteredOptions.map(option => (
            <label className={`select-option ${selectedValues.has(option.value) ? 'selected' : ''}`} key={option.value}>
              <input type="checkbox" checked={selectedValues.has(option.value)} onChange={() => toggle(option.value)} />
              <span>{option.label}</span>
            </label>
          ))}
          {filteredOptions.length === 0 && <div className="select-empty">无匹配选项</div>}
        </div>
      )}
    </div>
  );
}

function filterOptions(options: SelectOption[], query: string) {
  const keyword = query.trim().toLowerCase();
  if (!keyword) return options;
  return options.filter(option => `${option.label} ${option.keywords ?? ''}`.toLowerCase().includes(keyword));
}

function filterTableRows<T extends object>(rows: T[], query: string) {
  const keyword = query.trim().toLowerCase();
  if (!keyword) return rows;
  return rows.filter(row => Object.values(row as Record<string, unknown>).some(value => String(Array.isArray(value) ? value.join(' ') : value ?? '').toLowerCase().includes(keyword)));
}

function Modal({ title, open, onClose, children }: { title: string; open: boolean; onClose: () => void; children: ReactNode }) {
  if (!open) return null;
  return (
    <div className="modal-backdrop" role="presentation" onMouseDown={event => { if (event.target === event.currentTarget) onClose(); }}>
      <section className="modal-panel" role="dialog" aria-modal="true" aria-label={title}>
        <div className="modal-header">
          <h2>{title}</h2>
          <button type="button" className="ghost" onClick={onClose}>关闭</button>
        </div>
        {children}
      </section>
    </div>
  );
}

function SimpleTable({ rows, title, actions }: { rows: Entity[]; title?: string; actions?: (row: any) => any }) {
  const [query, setQuery] = useState('');
  const filteredRows = useMemo(() => filterTableRows(rows, query), [rows, query]);
  const columns = useMemo(() => Object.keys(rows[0] ?? {}).filter(key => typeof rows[0]?.[key] !== 'object').slice(0, 8), [rows]);
  return (
    <div className="table-wrap">
      {title && <h3>{title}</h3>}
      {rows.length > 0 && (
        <div className="table-toolbar">
          <input className="table-search" placeholder={`搜索${title ?? '当前列表'}`} value={query} onChange={event => setQuery(event.target.value)} />
          <span>{filteredRows.length}/{rows.length}</span>
        </div>
      )}
      <table>
        <thead><tr>{columns.map(column => <th key={column}>{labelOf(column)}</th>)}{actions && <th>操作</th>}</tr></thead>
        <tbody>{filteredRows.map((row, index) => <tr key={row.id ?? index}>{columns.map(column => <td key={column}>{String(row[column] ?? '')}</td>)}{actions && <td>{actions(row)}</td>}</tr>)}</tbody>
      </table>
    </div>
  );
}

function AwardTable({ rows, actions, onCertificateUpload }: { rows: AwardView[]; actions?: (row: AwardView) => any; onCertificateUpload?: (awardId: string, file?: File) => void }) {
  const [query, setQuery] = useState('');
  const filteredRows = useMemo(() => filterTableRows(rows, query), [rows, query]);
  return (
    <div className="table-wrap">
      {rows.length > 0 && (
        <div className="table-toolbar">
          <input className="table-search" placeholder="搜索获奖、团队、学生、教师" value={query} onChange={event => setQuery(event.target.value)} />
          <span>{filteredRows.length}/{rows.length}</span>
        </div>
      )}
      <table>
        <thead><tr><th>竞赛</th><th>赛道</th><th>别名</th><th>等级</th><th>奖项</th><th>主体</th><th>获奖人/团队</th><th>日期</th><th>地点</th><th>审核</th><th>证书</th>{actions && <th>操作</th>}</tr></thead>
        <tbody>{filteredRows.map(row => <tr key={row.id}>
          <td>{row.competitionName}</td><td>{row.trackName}</td><td>{row.competitionAlias || ''}</td><td>{row.competitionGradeLabel}</td><td>{row.awardLevelLabel}</td><td>{row.subjectTypeLabel}</td>
          <td>{row.studentName || row.teacherName || row.teamName}</td><td>{row.awardDate || ''}</td><td>{row.awardLocation || ''}</td><td>{row.auditStatusLabel}</td>
          <td>
            <div className="certificate-actions">
              {row.certificateId ? <button onClick={() => downloadApi(`/api/certificates/${row.certificateId}/download`, 'certificate')}>下载</button> : <span>无</span>}
              {onCertificateUpload && (
                <label className="file-control">
                  导入证书
                  <input
                    type="file"
                    accept=".pdf,.jpg,.jpeg,.png"
                    onChange={event => {
                      onCertificateUpload(row.id, event.currentTarget.files?.[0]);
                      event.currentTarget.value = '';
                    }}
                  />
                </label>
              )}
            </div>
          </td>
          {actions && <td>{actions(row)}</td>}
        </tr>)}</tbody>
      </table>
    </div>
  );
}

function Loading() {
  return <div className="loading">加载中...</div>;
}

function gradeOptions() {
  return [
    { value: 'FIRST_A', label: '一类A' }, { value: 'FIRST_B', label: '一类B' }, { value: 'SECOND_A', label: '二类A' },
    { value: 'SECOND_B', label: '二类B' }, { value: 'THIRD', label: '三类' }, { value: 'OTHER', label: '其他' }
  ];
}

function awardOptions() {
  return [
    { value: 'GRAND_PRIZE', label: '特等奖' }, { value: 'FIRST_PRIZE', label: '一等奖' }, { value: 'SECOND_PRIZE', label: '二等奖' },
    { value: 'THIRD_PRIZE', label: '三等奖' }, { value: 'EXCELLENCE', label: '优秀奖' }, { value: 'EXCELLENT_ADVISOR', label: '优秀指导老师奖' }
  ];
}

function subjectOptions() {
  return [{ value: 'STUDENT', label: '个人赛' }, { value: 'TEAM', label: '团队赛' }];
}

function labelOf(key: string) {
  const labels: Record<string, string> = {
    studentTotal: '学生总数', teacherTotal: '教师总数', competitionTotal: '竞赛总数', awardTotal: '获奖总数',
    pendingUserTotal: '待审核用户', pendingAwardTotal: '待审核申报', certificateTotal: '证书数量',
    firstCategoryAwardTotal: '一类竞赛', secondCategoryAwardTotal: '二类竞赛', firstAAwardTotal: '一类A',
    firstBAwardTotal: '一类B', secondAAwardTotal: '二类A', secondBAwardTotal: '二类B',
    personalAwardTotal: '个人赛', teamAwardTotal: '团队赛', teacherAwardTotal: '教师获奖'
  };
  return labels[key] ?? key;
}

export default App;
